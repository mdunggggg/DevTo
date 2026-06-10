package com.example.devto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.domain.model.Article
import com.example.domain.model.params.ArticleParam
import com.example.domain.usecase.OfflineFetchArticleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class DemoViewModel @Inject constructor(
    private val offlineFetchArticleUseCase: OfflineFetchArticleUseCase
) : ViewModel() {

    val selectedTag = MutableStateFlow("android")

    @OptIn(ExperimentalCoroutinesApi::class)
    val articles: Flow<PagingData<Article>> = selectedTag
        .flatMapLatest { tag ->
            offlineFetchArticleUseCase(ArticleParam(tag = tag, perPage = 20))
        }
        .cachedIn(viewModelScope)

    fun updateFilter(tag: String) {
        selectedTag.value = tag
    }
}