package com.example.domain.repo

import com.example.core.result.Result
import com.example.domain.model.Article
import com.example.domain.model.params.ArticleParam

interface ArticleRepository {
    suspend fun fetchArticles(params: ArticleParam) : Result<List<Article>, Throwable>
}