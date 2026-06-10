package com.example.data.pager

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.data.local.database.DevToDatabase
import com.example.data.local.model.ArticleWithRelations
import com.example.data.local.model.RemoteKey
import com.example.data.mapper.toEntity
import com.example.data.mapper.toEntityWithRelation
import com.example.data.remote.api.ArticleApi
import com.example.domain.model.Article
import com.example.domain.model.params.ArticleParam
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
internal class ArticleRemoteMediator(
    private val param: ArticleParam,
    private val articleApi: ArticleApi,
    private val database: DevToDatabase
) : RemoteMediator<Int, ArticleWithRelations>(){

    private val articleDao = database.articleDao()
    private val remoteKeyDao = database.remoteKeyDao()
    private val cacheKey = RemoteKey.buildKey(param)

    override suspend fun initialize(): InitializeAction {
        val remoteKey = remoteKeyDao.getRemoteKey(cacheKey)
        val isStale = remoteKey == null ||
                System.currentTimeMillis() - remoteKey.lastUpdated > CACHE_TIMEOUT

        return if (isStale) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleWithRelations>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> 1

                LoadType.PREPEND ->
                    return MediatorResult.Success(endOfPaginationReached = true)

                LoadType.APPEND -> {
                    val remoteKey = remoteKeyDao.getRemoteKey(cacheKey)
                        ?: return MediatorResult.Success(endOfPaginationReached = true)

                    remoteKey.nextPage
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }
            val responseFromAPI = articleApi.getArticles(
                param.copy(page = page).toQueryMap()
            )
            val endOfPaginationReached = responseFromAPI.size < param.perPage
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    articleDao.deleteByCacheKey(cacheKey)
                    remoteKeyDao.deleteByKey(cacheKey)
                }
                val nextPage = if (endOfPaginationReached) null else page + 1
                remoteKeyDao.insert(
                    RemoteKey(
                        cacheKey = cacheKey,
                        nextPage = nextPage,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
                articleDao.insertArticles(responseFromAPI.map {it.toEntityWithRelation(cacheKey)})
            }
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }

    companion object {
        const val CACHE_TIMEOUT = 30 * 60 * 1000L
    }

}