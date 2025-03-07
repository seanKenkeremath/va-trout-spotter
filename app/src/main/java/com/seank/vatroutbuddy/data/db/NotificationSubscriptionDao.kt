package com.seank.vatroutbuddy.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationSubscriptionDao {
    @Query("SELECT * FROM notification_subscriptions WHERE type = :type")
    fun getSubscriptionsByType(type: SubscriptionType): Flow<List<NotificationSubscriptionEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM notification_subscriptions WHERE type = :type AND value = :value)")
    fun isSubscribed(type: SubscriptionType, value: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSubscription(subscription: NotificationSubscriptionEntity)

    @Delete
    suspend fun removeSubscription(subscription: NotificationSubscriptionEntity)
} 