package com.example.data.repository

import com.example.core.result.Result
import com.example.data.remote.ArticleRemoteDataSource
import com.example.domain.model.Article
import com.example.domain.model.params.ArticleParam
import com.example.domain.repo.ArticleRepository

class ArticleRepositoryImpl(
    private val remoteDataSource: ArticleRemoteDataSource
): ArticleRepository {
    override suspend fun fetchArticles(params: ArticleParam): Result<List<Article>, Throwable> {
        // TODO:
    }
}