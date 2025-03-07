package com.seank.vatroutbuddy.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.seank.vatroutbuddy.data.repository.StockingRepository
import com.seank.vatroutbuddy.data.repository.NotificationSubscriptionRepository
import com.seank.vatroutbuddy.R
import androidx.core.app.NotificationCompat
import com.seank.vatroutbuddy.domain.model.StockingInfo
import com.seank.vatroutbuddy.notifications.NotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class StockingUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: StockingRepository,
    private val subscriptionRepository: NotificationSubscriptionRepository,
    private val notificationManager: NotificationManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            repository.fetchLatestStockings()
                .fold(
                    onSuccess = { stockings ->
                        // Get current subscriptions
                        val countySubscriptions = subscriptionRepository.getCountySubscriptions().first()
                        val waterbodySubscriptions = subscriptionRepository.getWaterbodySubscriptions().first()
                        
                        // Check each new stocking against subscriptions
                        stockings.forEach { stocking ->
                            val shouldNotify = countySubscriptions.any { it.value == stocking.county } ||
                                    waterbodySubscriptions.any { it.value == stocking.waterbody }
                            
                            if (shouldNotify) {
                                sendStockingNotification(stocking)
                            }
                        }
                        Result.success()
                    },
                    onFailure = { Result.retry() }
                )
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendStockingNotification(stocking: StockingInfo) {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New Stocking Alert!")
            .setContentText("${stocking.waterbody} has been stocked!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(stocking.id.toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "stocking_notifications"
    }
} 