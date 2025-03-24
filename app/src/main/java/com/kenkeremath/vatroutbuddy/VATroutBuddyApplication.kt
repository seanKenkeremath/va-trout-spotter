package com.kenkeremath.vatroutbuddy

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.kenkeremath.vatroutbuddy.notifications.NotificationManager
import com.kenkeremath.vatroutbuddy.worker.StockingWorkScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class VATroutBuddyApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var stockingWorkScheduler: StockingWorkScheduler

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        notificationManager.createNotificationChannel()
        stockingWorkScheduler.schedulePeriodicWork()
        stockingWorkScheduler.scheduleHistoricalDataDownload()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
}