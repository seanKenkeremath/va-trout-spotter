package com.kenkeremath.vatroutbuddy.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        StockingEntity::class,
        NotificationSubscriptionEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stockingDao(): StockingDao
    abstract fun notificationSubscriptionDao(): NotificationSubscriptionDao
} 