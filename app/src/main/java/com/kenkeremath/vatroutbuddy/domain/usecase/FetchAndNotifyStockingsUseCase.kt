package com.kenkeremath.vatroutbuddy.domain.usecase

import com.kenkeremath.vatroutbuddy.data.repository.NotificationSubscriptionRepository
import com.kenkeremath.vatroutbuddy.data.repository.StockingRepository
import com.kenkeremath.vatroutbuddy.domain.model.StockingInfo
import com.kenkeremath.vatroutbuddy.notifications.NotificationManager
import com.kenkeremath.vatroutbuddy.worker.StockingUpdateWorker
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import androidx.core.app.NotificationCompat
import com.kenkeremath.vatroutbuddy.R

class FetchAndNotifyStockingsUseCase @Inject constructor(
    private val stockingRepository: StockingRepository,
    private val subscriptionRepository: NotificationSubscriptionRepository,
    private val notificationManager: NotificationManager
) {
    /**
     * Fetches the latest stockings and sends notifications for any that match user subscriptions.
     * @return Result containing the list of new stockings if successful, or an exception if failed
     */
    suspend fun execute(): Result<List<StockingInfo>> {
        return try {
            stockingRepository.fetchLatestStockings()
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
                        Result.success(stockings)
                    },
                    onFailure = { Result.failure(it) }
                )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun sendStockingNotification(stocking: StockingInfo) {
        val notification = NotificationCompat.Builder(notificationManager.context, NotificationManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notificationManager.context.getString(R.string.notification_stocking_title))
            .setContentText(notificationManager.context.getString(
                R.string.notification_stocking_content, 
                stocking.waterbody
            ))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(stocking.id.toInt(), notification)
    }
} 