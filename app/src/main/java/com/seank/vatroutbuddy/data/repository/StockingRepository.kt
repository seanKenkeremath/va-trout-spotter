package com.seank.vatroutbuddy.data.repository

import com.seank.vatroutbuddy.data.db.StockingDao
import com.seank.vatroutbuddy.data.db.StockingEntity
import com.seank.vatroutbuddy.data.network.StockingNetworkDataSource
import com.seank.vatroutbuddy.domain.model.StockingInfo
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockingRepository @Inject constructor(
    private val networkDataSource: StockingNetworkDataSource,
    private val stockingDao: StockingDao
) {

    // Load stocking data from the network and store it in the database
    suspend fun fetchStockingsInDateRange(
        startDate: LocalDate,
        endDate: LocalDate? = null
    ): Result<List<StockingInfo>> {
        return try {
            val entities = networkDataSource.fetchStockings(
                startDate = startDate,
                endDate = endDate
            )
                .getOrThrow()
                .map {
                    StockingEntity.fromStockingInfo(it, LocalDateTime.now())
                }
            val stockingInfos = stockingDao.insertAndReturnStockings(entities)
            Result.success(stockingInfos.map { it.toStockingInfo() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Load the latest stocking data from the network and store it in the database
    suspend fun fetchLatestStockings(): Result<List<StockingInfo>> {
        val lastStockingDate = stockingDao.getMostRecentStockingDate()
        val startDate = if (lastStockingDate != null) {
            // Start from the day before the last stocking to be safe
            lastStockingDate.minusDays(1)
        } else {
            LocalDate.now().minusMonths(DEFAULT_MONTHS_PAST)
        }
        return fetchStockingsInDateRange(startDate)
    }

    suspend fun loadCachedStockings(pageSize: Int): Result<StockingsListPage> {
        return try {
            val stockings = stockingDao.getMostRecentStockings(limit = pageSize + 1)
            val hasMore = stockings.size > pageSize
            val pageStockings = stockings.take(pageSize)
            Result.success(
                StockingsListPage(
                    stockings = pageStockings.map { it.toStockingInfo() },
                    hasMore = hasMore
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadMoreCachedStockings(
        lastDate: LocalDate,
        lastWaterbody: String,
        lastId: Long,
        pageSize: Int
    ): Result<StockingsListPage> {
        return try {
            val stockings = stockingDao.getStockingsPaged(
                lastDate = lastDate,
                lastWaterbody = lastWaterbody,
                lastId = lastId,
                pageSize = pageSize + 1
            )
            val hasMore = stockings.size > pageSize
            val pageStockings = stockings.take(pageSize)
            Result.success(
                StockingsListPage(
                    stockings = pageStockings.map { it.toStockingInfo() },
                    hasMore = hasMore
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    class StockingsListPage(
        val stockings: List<StockingInfo>,
        val hasMore: Boolean,
    )

    companion object {
        const val DEFAULT_MONTHS_PAST = 12L
    }
} 