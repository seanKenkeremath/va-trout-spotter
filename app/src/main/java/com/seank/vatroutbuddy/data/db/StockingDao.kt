package com.seank.vatroutbuddy.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface StockingDao {
    @Query("SELECT * FROM stockings WHERE date >= :startDate")
    fun getStockingsAfter(startDate: LocalDate): Flow<List<StockingEntity>>

    @Query("SELECT MAX(lastUpdated) FROM stockings")
    fun getLastUpdateTime(): Flow<LocalDateTime?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stockings: List<StockingEntity>)

    @Query("SELECT MAX(date) FROM stockings")
    suspend fun getMostRecentStockingDate(): LocalDate?
} 