package com.example.data.di

import android.content.Context
import com.example.data.local.database.DevToDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDevToDatabase(
        @ApplicationContext context: Context
    ) : DevToDatabase = DevToDatabase.getInstance(context)
}