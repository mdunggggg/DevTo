package com.example.domain.repo

import androidx.paging.PagingData
import com.example.core.result.Result
import com.example.domain.model.Article
import com.example.domain.model.params.ArticleParam
import kotlinx.coroutines.flow.Flow

interface ArticleRepository {
    suspend fun fetchArticles(params: ArticleParam) : Result<List<Article>, Throwable>

    fun fetchOfflineArticles(param: ArticleParam): Flow<PagingData<Article>>

    suspend fun deleteArticlesNotInCache()
}