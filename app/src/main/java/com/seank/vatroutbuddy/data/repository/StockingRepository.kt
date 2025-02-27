package com.seank.vatroutbuddy.data.repository

import com.seank.vatroutbuddy.data.db.StockingDao
import com.seank.vatroutbuddy.data.db.StockingEntity
import com.seank.vatroutbuddy.data.network.StockingNetworkDataSource
import com.seank.vatroutbuddy.domain.model.StockingInfo
import kotlinx.coroutines.flow.Flow
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
        return stockingDao.getStockingsAfter(startDate)
            .map { entities -> entities.map { it.toStockingInfo() } }
    }

    fun getLastUpdateTime(): Flow<LocalDateTime?> = stockingDao.getLastUpdateTime()

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