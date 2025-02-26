package com.seank.vatroutbuddy.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface StockingDao {
    @Query("SELECT * FROM stockings WHERE date >= :startDate ORDER BY date DESC")
    fun getStockingsAfter(startDate: LocalDate): Flow<List<StockingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stockings: List<StockingEntity>)
} 