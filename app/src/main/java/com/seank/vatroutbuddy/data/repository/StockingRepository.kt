package com.seank.vatroutbuddy.data.repository

import com.seank.vatroutbuddy.data.db.StockingDao
import com.seank.vatroutbuddy.data.db.StockingEntity
import com.seank.vatroutbuddy.data.network.StockingNetworkDataSource
import com.seank.vatroutbuddy.data.preferences.AppPreferences
import com.seank.vatroutbuddy.di.IoDispatcher
import com.seank.vatroutbuddy.domain.model.StockingInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import com.seank.vatroutbuddy.AppConfig
import com.seank.vatroutbuddy.domain.model.StockingsListPage

@Singleton
class StockingRepository @Inject constructor(
    private val networkDataSource: StockingNetworkDataSource,
    private val stockingDao: StockingDao,
    private val preferences: AppPreferences,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    val hasHistoricalData = preferences.hasDownloadedHistoricalData

    // Load stocking data from the network and store it in the database
    suspend fun fetchStockingsInDateRange(
        startDate: LocalDate,
        endDate: LocalDate? = null
    ): Result<List<StockingInfo>> = withContext(ioDispatcher) {
        try {
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
    suspend fun fetchLatestStockings(): Result<List<StockingInfo>> = withContext(ioDispatcher) {
        val lastStockingDate = stockingDao.getMostRecentStockingDate()
        val startDate = if (lastStockingDate != null) {
            // Start from the day before the last stocking to be safe
            lastStockingDate.minusDays(1)
        } else {
            LocalDate.now().minusMonths(AppConfig.DEFAULT_MONTHS_PAST)
        }
        fetchStockingsInDateRange(startDate)
    }

    suspend fun loadSavedStockings(pageSize: Int): Result<StockingsListPage> =
        withContext(ioDispatcher) {
            try {
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

    suspend fun loadMoreSavedStockings(
        lastDate: LocalDate,
        lastWaterbody: String,
        lastId: Long,
        pageSize: Int
    ): Result<StockingsListPage> = withContext(ioDispatcher) {
        try {
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

    suspend fun fetchHistoricalData(): Result<List<StockingInfo>> = withContext(ioDispatcher) {
        try {
            val startDate = AppConfig.HISTORICAL_DATA_START_DATE
            val endDate = stockingDao.getEarliestStockingDate() ?: LocalDate.now()

            fetchStockingsInDateRange(startDate, endDate)
                .also { result ->
                    if (result.isSuccess) {
                        preferences.setHistoricalDataDownloaded(true)
                    }
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}