package com.seank.vatroutbuddy.worker

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockingWorkScheduler @Inject constructor(
    private val workManager: WorkManager
) {
    fun schedulePeriodicWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<StockingUpdateWorker>(
            repeatInterval = 6,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    companion object {
        private const val WORK_NAME = "stocking_update_work"
    }
} 