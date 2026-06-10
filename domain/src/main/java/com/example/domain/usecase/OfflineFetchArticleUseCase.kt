package com.example.domain.usecase

import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.example.core.result.Result
import com.example.core.usecase.FlowUseCase
import com.example.core.usecase.UseCase
import com.example.domain.model.Article
import com.example.domain.model.params.ArticleParam
import com.example.domain.repo.ArticleRepository
import kotlinx.coroutines.flow.Flow

class OfflineFetchArticleUseCase(
    private val articleRepository: ArticleRepository
) : FlowUseCase<ArticleParam, PagingData<Article>>() {

    override suspend fun invoke(input: ArticleParam): Flow<PagingData<Article>> {
        validate(input)
        return articleRepository.fetchOfflineArticles(input)
    }

    private fun validate(input: ArticleParam) {
        require(input.page >= 1) { "page must be >= 1" }
        require(input.perPage in 1..50) { "perPage must be in 1..50" }
    }
}