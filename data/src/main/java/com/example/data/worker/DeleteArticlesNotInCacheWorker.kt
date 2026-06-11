package com.example.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.example.domain.repo.ArticleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class DeleteArticlesNotInCacheWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val articleRepository: ArticleRepository
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        articleRepository.deleteArticlesNotInCache()
        return Result.success()
    }

    companion object {
        fun createPeriodRequest() = PeriodicWorkRequestBuilder<DeleteArticlesNotInCacheWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        ).build()
    }
}
