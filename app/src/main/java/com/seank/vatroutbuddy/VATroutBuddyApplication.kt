package com.seank.vatroutbuddy

import android.app.Application
import com.seank.vatroutbuddy.worker.StockingWorkScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class VATroutBuddyApplication : Application() {
    @Inject
    lateinit var stockingWorkScheduler: StockingWorkScheduler

    override fun onCreate() {
        super.onCreate()
        stockingWorkScheduler.schedulePeriodicWork()
        stockingWorkScheduler.scheduleHistoricalDataDownload()
    }
} 