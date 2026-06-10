package com.example.data.di

import com.example.data.repository.ArticleRepositoryImpl
import com.example.domain.repo.ArticleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindArticleRepository(
        repositoryImpl: ArticleRepositoryImpl
    ) : ArticleRepository
}