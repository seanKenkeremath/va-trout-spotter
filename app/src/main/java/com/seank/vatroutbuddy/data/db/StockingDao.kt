package com.seank.vatroutbuddy.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface StockingDao {

    @Query("SELECT * FROM stockings ORDER BY date DESC, waterbody, id LIMIT :limit")
    suspend fun getMostRecentStockings(limit: Int): List<StockingEntity>

    @Query("SELECT MAX(date) FROM stockings")
    suspend fun getMostRecentStockingDate(): LocalDate?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStockings(stockings: List<StockingEntity>): List<Long>

    @Query("SELECT * FROM stockings WHERE id IN (:ids)")
    suspend fun getStockingsById(ids: List<Long>): List<StockingEntity>

    @Transaction
    suspend fun insertAndReturnStockings(entities: List<StockingEntity>): List<StockingEntity> {
        val ids = insertStockings(entities)
        return getStockingsById(ids)
    }

    @Query("""
    SELECT * FROM stockings
    WHERE (date < :lastDate)
       OR (date = :lastDate AND waterbody > :lastWaterbody)
       OR (date = :lastDate AND waterbody = :lastWaterbody AND id > :lastId)
    ORDER BY date DESC, waterbody, id
    LIMIT :pageSize
""")
    suspend fun getStockingsPaged(
        lastDate: LocalDate,
        lastWaterbody: String,
        lastId: Long,
        pageSize: Int
    ): List<StockingEntity>
} 