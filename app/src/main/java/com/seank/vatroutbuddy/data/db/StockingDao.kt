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
    @Query("SELECT * FROM stockings WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getStockingsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<StockingEntity>>

    @Query("SELECT * FROM stockings WHERE date <= :date ORDER BY date DESC, county ASC LIMIT :limit")
    fun getStockingsBeforeDate(date: LocalDate, limit: Int): Flow<List<StockingEntity>>

    @Query("SELECT COUNT(*) FROM stockings WHERE date <= :date")
    suspend fun countStockingsBeforeDate(date: LocalDate): Int

    @Query("SELECT MAX(lastUpdated) FROM stockings")
    fun getLastUpdateTime(): Flow<LocalDateTime?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stockings: List<StockingEntity>)

    @Query("SELECT MAX(date) FROM stockings")
    suspend fun getMostRecentStockingDate(): LocalDate?
} 