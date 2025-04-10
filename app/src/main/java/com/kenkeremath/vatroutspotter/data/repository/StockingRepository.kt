package com.kenkeremath.vatroutspotter.data.repository

import StockingFilters
import com.kenkeremath.vatroutspotter.AppConfig
import com.kenkeremath.vatroutspotter.data.db.StockingDao
import com.kenkeremath.vatroutspotter.data.db.StockingEntity
import com.kenkeremath.vatroutspotter.data.network.StockingNetworkDataSource
import com.kenkeremath.vatroutspotter.data.preferences.AppPreferences
import com.kenkeremath.vatroutspotter.di.IoDispatcher
import com.kenkeremath.vatroutspotter.domain.error.toDomainException
import com.kenkeremath.vatroutspotter.domain.model.StockingInfo
import com.kenkeremath.vatroutspotter.domain.model.StockingsListPage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockingRepository @Inject constructor(
    private val networkDataSource: StockingNetworkDataSource,
    private val stockingDao: StockingDao,
    private val preferences: AppPreferences,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    val hasInitialData = preferences.hasDownloadedInitialData
    val hasHistoricalData = preferences.hasDownloadedHistoricalData

    // Load stocking data from the network and store it in the database
    suspend fun fetchStockingsInDateRange(
        startDate: LocalDate,
        endDate: LocalDate? = null
    ): Result<List<StockingInfo>> = withContext(ioDispatcher) {
        try {
            val entities = networkDataSource.fetchStockings(
                startDate = startDate,
                endDate = endDate,
            )
                .getOrThrow()
                .map {
                    StockingEntity.fromStockingInfo(it, LocalDateTime.now())
                }

            val newIds = stockingDao.insertStockings(entities)
            val stockingInfos = stockingDao.getStockingsSinceDate(startDate).filter {
                it.id in newIds // Ensure we only return newly inserted stockings
            }
            Result.success(stockingInfos.map { it.toStockingInfo() })
        } catch (e: Exception) {
            Result.failure(e.toDomainException())
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
        val result = fetchStockingsInDateRange(startDate)
        if (result.isSuccess) {
            preferences.setInitialDataDownloaded(true)
        }
        result
    }

    suspend fun loadSavedStockings(
        pageSize: Int,
        stockingFilters: StockingFilters? = null,
    ): Result<StockingsListPage> =
        withContext(ioDispatcher) {
            try {
                val countiesQuery = if (stockingFilters?.counties?.isNotEmpty() == true) {
                    stockingFilters.counties.toList()
                } else {
                    null
                }

                val stockings = stockingDao.getMostRecentStockings(
                    limit = pageSize + 1,
                    counties = countiesQuery,
                    isNationalForest = stockingFilters?.isNationalForest,
                    isHeritageDayWater = stockingFilters?.isHeritageDayWater,
                    isNsf = stockingFilters?.isNsf,
                    isDelayedHarvest = stockingFilters?.isDelayedHarvest,
                    searchTerm = stockingFilters?.searchTerm
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
                Result.failure(e.toDomainException())
            }
        }

    suspend fun loadMoreSavedStockings(
        lastDate: LocalDate,
        lastWaterbody: String,
        lastId: Long,
        pageSize: Int,
        stockingFilters: StockingFilters? = null,
    ): Result<StockingsListPage> = withContext(ioDispatcher) {
        try {
            val countiesQuery = if (stockingFilters?.counties?.isNotEmpty() == true) {
                stockingFilters.counties.toList()
            } else {
                null
            }
            val stockings = stockingDao.getStockingsPaged(
                lastDate = lastDate,
                lastWaterbody = lastWaterbody,
                lastId = lastId,
                pageSize = pageSize + 1,
                counties = countiesQuery,
                isNationalForest = stockingFilters?.isNationalForest,
                isHeritageDayWater = stockingFilters?.isHeritageDayWater,
                isNsf = stockingFilters?.isNsf,
                isDelayedHarvest = stockingFilters?.isDelayedHarvest,
                searchTerm = stockingFilters?.searchTerm
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
            Result.failure(e.toDomainException())
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
            Result.failure(e.toDomainException())
        }
    }


    suspend fun getStockingsByWaterbody(
        waterbody: String,
        limit: Int = 10,
    ): Result<List<StockingInfo>> = withContext(ioDispatcher) {
        try {
            val stockings = stockingDao.getStockingsByWaterbody(waterbody, limit)
            Result.success(stockings.map { it.toStockingInfo() })
        } catch (e: Exception) {
            Result.failure(e.toDomainException())
        }
    }

    suspend fun getAllCounties(): List<String> = withContext(ioDispatcher) {
        stockingDao.getAllCounties()
    }

    suspend fun getAllWaterbodies(): List<String> = withContext(ioDispatcher) {
        stockingDao.getAllWaterbodies()
    }

    suspend fun getAllCategories(): List<String> = withContext(ioDispatcher) {
        stockingDao.getAllCategories()
    }
}