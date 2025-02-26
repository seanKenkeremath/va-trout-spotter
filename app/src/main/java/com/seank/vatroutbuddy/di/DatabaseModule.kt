package com.seank.vatroutbuddy.di

import android.content.Context
import androidx.room.Room
import com.seank.vatroutbuddy.data.db.AppDatabase
import com.seank.vatroutbuddy.data.db.StockingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "vatroutbuddy.db"
        ).build()
    }

    @Provides
    fun provideStockingDao(database: AppDatabase): StockingDao {
        return database.stockingDao()
    }
} 