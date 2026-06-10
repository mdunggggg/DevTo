package com.example.data.di

import com.example.data.remote.api.ArticleApi
import com.example.domain.model.Article
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {
    @Provides
    @Singleton
    fun provideArticleApi() : ArticleApi = ArticleApi.build("https://dev.to/api/")
}