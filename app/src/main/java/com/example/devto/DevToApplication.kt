package com.example.devto

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.example.data.worker.DeleteArticlesNotInCacheWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DevToApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            DELETE_ORPHAN_ARTICLES_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            DeleteArticlesNotInCacheWorker.createPeriodRequest()
        )
    }

    companion object {
        private const val DELETE_ORPHAN_ARTICLES_WORK = "delete_orphan_articles"
    }
}
