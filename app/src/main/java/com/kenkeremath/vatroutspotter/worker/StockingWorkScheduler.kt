package com.kenkeremath.vatroutspotter.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.ExistingWorkPolicy
import com.kenkeremath.vatroutspotter.AppConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockingWorkScheduler @Inject constructor(
    @ApplicationContext private val appContext: Context
) {

    private val workManager: WorkManager
        get() = WorkManager.getInstance(appContext)

    fun schedulePeriodicWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED) // Require WiFi
            .build()

        val workRequest = PeriodicWorkRequestBuilder<StockingUpdateWorker>(
            repeatInterval = AppConfig.BACKGROUND_FETCH_FREQUENCY_HOURS,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
        )
            .setInitialDelay(AppConfig.BACKGROUND_FETCH_DELAY_HOURS, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun scheduleHistoricalDataDownload() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED) // Require WiFi
            .build()

        val workRequest = OneTimeWorkRequestBuilder<HistoricalDataWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            HISTORICAL_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

    companion object {
        private const val WORK_NAME = "stocking_update_work"
        private const val HISTORICAL_WORK_NAME = "historical_data_work"
    }
} 