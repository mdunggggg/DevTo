package com.example.domain.usecase

import com.example.core.result.Result
import com.example.core.usecase.UseCase
import com.example.domain.model.Article
import com.example.domain.model.params.ArticleParam
import com.example.domain.repo.ArticleRepository

class FetchArticleUseCase(
    private val articleRepository: ArticleRepository
) : UseCase<ArticleParam, Result<List<Article>, Throwable>>() {

    override suspend fun invoke(input: ArticleParam): Result<List<Article>, Throwable> {
        return articleRepository.fetchArticles(input)
    }
}