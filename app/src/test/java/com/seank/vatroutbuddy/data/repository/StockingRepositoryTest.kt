package com.seank.vatroutbuddy.data.repository

import com.seank.vatroutbuddy.data.db.StockingDao
import com.seank.vatroutbuddy.data.network.StockingNetworkDataSource
import com.seank.vatroutbuddy.domain.model.StockingInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class StockingRepositoryTest {
    private lateinit var repository: StockingRepository
    private lateinit var networkDataSource: StockingNetworkDataSource
    private lateinit var stockingDao: StockingDao

    @Before
    fun setup() {
        networkDataSource = mockk()
        stockingDao = mockk(relaxed = true)
        repository = StockingRepository(networkDataSource, stockingDao)
    }

    @Test
    fun `refreshSinceLastStocking uses most recent stocking date minus one day when available`() =
        runTest {
            val lastStockingDate = LocalDate.now().minusDays(5)
            val expectedStartDate = lastStockingDate.minusDays(1)
            val mockStockings = listOf(mockk<StockingInfo>())

            coEvery { stockingDao.getMostRecentStockingDate() } returns lastStockingDate
            coEvery { networkDataSource.fetchStockings(expectedStartDate) } returns Result.success(
                mockStockings
            )

            repository.fetchLatestStockings()

            coVerify { networkDataSource.fetchStockings(expectedStartDate) }
        }

    @Test
    fun `refreshSinceLastStocking uses default months past when no stockings exist`() = runTest {
        val expectedStartDate = LocalDate.now().minusMonths(StockingRepository.DEFAULT_MONTHS_PAST)
        val mockStockings = listOf(mockk<StockingInfo>())

        coEvery { stockingDao.getMostRecentStockingDate() } returns null
        coEvery { networkDataSource.fetchStockings(expectedStartDate) } returns Result.success(
            mockStockings
        )

        repository.fetchLatestStockings()

        coVerify { networkDataSource.fetchStockings(expectedStartDate) }
    }

    @Test
    fun `refreshSinceLastStocking returns failure when network call fails`() = runTest {
        val expectedStartDate = LocalDate.now().minusMonths(StockingRepository.DEFAULT_MONTHS_PAST)
        val expectedException = Exception("Network error")

        coEvery { stockingDao.getMostRecentStockingDate() } returns null
        coEvery { networkDataSource.fetchStockings(expectedStartDate) } returns Result.failure(
            expectedException
        )

        val result = repository.fetchLatestStockings()

        assert(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
    }

    @Test
    fun `refreshSinceDate updates database with fetched stockings`() = runTest {
        val startDate = LocalDate.now().minusDays(5)

        coEvery { networkDataSource.fetchStockings(startDate) } returns Result.success(
            listOf(
                StockingInfo(
                    id = 1,
                    date = LocalDate.now(),
                    county = "Test County",
                    waterbody = "Test Waterbody",
                    category = "A",
                    species = listOf("Rainbow Trout"),
                    isNationalForest = false
                )
            )
        )

        repository.fetchStockingsInDateRange(startDate)

        coVerify { stockingDao.insertAndReturnStockings(any()) }
    }
} 