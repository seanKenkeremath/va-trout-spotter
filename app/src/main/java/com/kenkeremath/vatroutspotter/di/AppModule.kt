package com.kenkeremath.vatroutspotter.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.kenkeremath.vatroutspotter.data.db.AppDatabase
import com.kenkeremath.vatroutspotter.data.db.StockingDao
import com.kenkeremath.vatroutspotter.data.db.NotificationSubscriptionDao
import com.kenkeremath.vatroutspotter.permissions.AndroidPermissionsManager
import com.kenkeremath.vatroutspotter.permissions.PermissionsManager
import com.kenkeremath.vatroutspotter.util.Clock
import com.kenkeremath.vatroutspotter.util.DefaultClock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideClock(): Clock = DefaultClock()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "vatroutspotter.db"
        ).build()
    }

    @Provides
    fun provideStockingDao(database: AppDatabase): StockingDao {
        return database.stockingDao()
    }

    @Provides
    fun provideNotificationSubscriptionDao(database: AppDatabase): NotificationSubscriptionDao {
        return database.notificationSubscriptionDao()
    }

    @Provides
    @Singleton
    fun providesDatastore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    @Provides
    @IoDispatcher
    fun provideIoDispatcher() : CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher() : CoroutineDispatcher = Dispatchers.Default

    @Provides
    @MainDispatcher
    fun provideMainDispatcher() : CoroutineDispatcher = Dispatchers.Main

    @Provides
    @Singleton
    fun providePermissionsManager(
        @ApplicationContext context: Context
    ): PermissionsManager = AndroidPermissionsManager(context)
} 