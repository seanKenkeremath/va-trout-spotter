package com.kenkeremath.vatroutspotter.data.repository

import com.kenkeremath.vatroutspotter.data.db.NotificationSubscriptionDao
import com.kenkeremath.vatroutspotter.data.db.NotificationSubscriptionEntity
import com.kenkeremath.vatroutspotter.data.db.SubscriptionType
import com.kenkeremath.vatroutspotter.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSubscriptionRepository @Inject constructor(
    private val subscriptionDao: NotificationSubscriptionDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    fun getCountySubscriptions(): Flow<List<NotificationSubscriptionEntity>> =
        subscriptionDao.getSubscriptionsByType(SubscriptionType.COUNTY)

    fun getWaterbodySubscriptions(): Flow<List<NotificationSubscriptionEntity>> =
        subscriptionDao.getSubscriptionsByType(SubscriptionType.WATERBODY)

    fun isSubscribedToCounty(county: String): Flow<Boolean> =
        subscriptionDao.isSubscribed(SubscriptionType.COUNTY, county)

    fun isSubscribedToWaterbody(waterbody: String): Flow<Boolean> =
        subscriptionDao.isSubscribed(SubscriptionType.WATERBODY, waterbody)

    suspend fun toggleCountySubscription(county: String, subscribe: Boolean) {
        withContext(ioDispatcher) {
            val subscription = NotificationSubscriptionEntity(SubscriptionType.COUNTY, county)
            if (subscribe) {
                subscriptionDao.addSubscription(subscription)
            } else {
                subscriptionDao.removeSubscription(subscription)
            }
        }
    }

    suspend fun toggleWaterbodySubscription(waterbody: String, subscribe: Boolean) {
        withContext(ioDispatcher) {
            val subscription = NotificationSubscriptionEntity(SubscriptionType.WATERBODY, waterbody)
            if (subscribe) {
                subscriptionDao.addSubscription(subscription)
            } else {
                subscriptionDao.removeSubscription(subscription)
            }
        }
    }
} 