package com.seank.vatroutbuddy.data.repository

import com.seank.vatroutbuddy.data.db.StockingDao
import com.seank.vatroutbuddy.data.db.StockingEntity
import com.seank.vatroutbuddy.data.network.StockingNetworkDataSource
import com.seank.vatroutbuddy.domain.model.StockingInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockingRepository @Inject constructor(
    private val networkDataSource: StockingNetworkDataSource,
    private val stockingDao: StockingDao
) {
    fun getRecentStockings(startDate: LocalDate): Flow<List<StockingInfo>> {
        return stockingDao.getStockingsInRange(startDate, LocalDate.now())
            .map { entities -> entities.map { it.toStockingInfo() } }
    }

    fun getLastUpdateTime(): Flow<LocalDateTime?> = stockingDao.getLastUpdateTime()

    // TODO: Check how many more stockings there are

    suspend fun loadStockingsBeforeDate(date: LocalDate, limit: Int): Result<List<StockingInfo>> {
        return try {
            val stockings = stockingDao.getStockingsBeforeDate(date, limit).first()
            Result.success(stockings.map { it.toStockingInfo() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun countStockingsBeforeDate(date: LocalDate): Int {
        return stockingDao.countStockingsBeforeDate(date)
    }

    suspend fun refreshSinceDate(startDate: LocalDate): Result<Unit> {
        return try {
            networkDataSource.fetchStockings(startDate)
                .map { stockings ->
                    val entities = stockings.map { StockingEntity.fromStockingInfo(it, LocalDateTime.now()) }
                    stockingDao.insertAll(entities)
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshSinceLastStocking(): Result<Unit> {
        val lastStockingDate = stockingDao.getMostRecentStockingDate()
        val startDate = if (lastStockingDate != null) {
            // Start from the day before the last stocking to be safe
            lastStockingDate.minusDays(1)
        } else {
            LocalDate.now().minusMonths(DEFAULT_MONTHS_PAST)
        }
        return refreshSinceDate(startDate)
    }

    companion object {
        const val DEFAULT_MONTHS_PAST = 6L
    }
} 