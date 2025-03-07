package com.seank.vatroutbuddy.data.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "notification_subscriptions",
    primaryKeys = ["type", "value"],
    indices = [Index(value = ["type", "value"], unique = true)]
)
data class NotificationSubscriptionEntity(
    val type: SubscriptionType,
    val value: String, // county name or waterbody name
)

enum class SubscriptionType {
    COUNTY,
    WATERBODY
} 