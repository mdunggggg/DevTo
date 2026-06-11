package com.example.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.core.result.Result
import com.example.data.local.database.DevToDatabase
import com.example.data.local.model.ArticleWithRelations
import com.example.data.local.model.RemoteKey
import com.example.data.mapper.toDomain
import com.example.data.pager.ArticleRemoteMediator
import com.example.data.remote.api.ArticleApi
import com.example.domain.model.Article
import com.example.domain.model.params.ArticleParam
import com.example.domain.repo.ArticleRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ArticleRepositoryImpl @Inject constructor(
    private val articleApi: ArticleApi,
    private val database: DevToDatabase
): ArticleRepository {
    override suspend fun fetchArticles(params: ArticleParam): Result<List<Article>, Throwable> {
        return Result.failure(Exception("TODO"))
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun fetchOfflineArticles(param: ArticleParam): Flow<PagingData<Article>> {
        val cacheKey = RemoteKey.buildKey(param)
        return Pager(
            config = PagingConfig(
                pageSize = param.perPage,
                initialLoadSize = param.perPage,
                prefetchDistance = 2,
                enablePlaceholders = false
            ),
            remoteMediator = ArticleRemoteMediator(
                param = param,
                articleApi = articleApi,
                database = database
            ),
            pagingSourceFactory = {
                database.articleDao().getArticlesByCacheKey(cacheKey)
            }
        ).flow.map { pagingData ->
            pagingData.map(ArticleWithRelations::toDomain)
        }
    }
}