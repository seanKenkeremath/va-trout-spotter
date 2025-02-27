package com.seank.vatroutbuddy.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class StockingDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var stockingDao: StockingDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        stockingDao = database.stockingDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `getStockingsInRange returns stockings in specified date range in descending order`() = runTest {
        val today = LocalDate.now()
        val stockings = listOf(
            createStocking(1, today, "Lake A"),
            createStocking(2, today.minusDays(1), "Lake B"),
            createStocking(3, today.minusDays(7), "Lake C"),
            createStocking(4, today.minusDays(30), "Lake D")
        )
        stockingDao.insertAll(stockings)

        val result = stockingDao.getStockingsInRange(
            startDate = today.minusDays(7),
            endDate = today
        ).first()

        assertEquals(3, result.size)
        assertEquals(listOf("Lake A", "Lake B", "Lake C"), result.map { it.waterbody })
    }

    @Test
    fun `getStockingsBeforeDate returns stockings before specified date in descending order`() = runTest {
        val today = LocalDate.now()
        val stockings = listOf(
            createStocking(1, today, "Lake A"),
            createStocking(2, today.minusDays(1), "Lake B"),
            createStocking(3, today.minusDays(7), "Lake C"),
            createStocking(4, today.minusDays(30), "Lake D")
        )
        stockingDao.insertAll(stockings)

        val result = stockingDao.getStockingsBeforeDate(
            date = today.minusDays(1),
            limit = 10
        ).first()

        assertEquals(3, result.size)
        assertEquals(listOf("Lake B", "Lake C", "Lake D"), result.map { it.waterbody })
    }

    @Test
    fun `getStockingsBeforeDate with limit returns stockings before specified date in descending order`() = runTest {
        val today = LocalDate.now()
        val stockings = listOf(
            createStocking(1, today, "Lake A"),
            createStocking(2, today.minusDays(1), "Lake B"),
            createStocking(3, today.minusDays(7), "Lake C"),
            createStocking(4, today.minusDays(30), "Lake D")
        )
        stockingDao.insertAll(stockings)

        val result = stockingDao.getStockingsBeforeDate(
            date = today.minusDays(1),
            limit = 2
        ).first()

        assertEquals(2, result.size)
        assertEquals(listOf("Lake B", "Lake C"), result.map { it.waterbody })
    }

    @Test
    fun `countStockingsBeforeDate returns correct count of stockings before specified date`() = runTest {
        val today = LocalDate.now()
        val stockings = listOf(
            createStocking(1, today, "Lake A"),
            createStocking(2, today.minusDays(1), "Lake B"),
            createStocking(3, today.minusDays(7), "Lake C")
        )
        stockingDao.insertAll(stockings)

        val count = stockingDao.countStockingsBeforeDate(today.minusDays(1))

        assertEquals(2, count)
    }

    @Test
    fun `insertAll replaces duplicates based on waterbody and date`() = runTest {
        val date = LocalDate.now()
        val original = createStocking(1, date, "Lake A", species = listOf("Rainbow"))
        val duplicate = createStocking(2, date, "Lake A", species = listOf("Brook", "Brown"))

        stockingDao.insertAll(listOf(original))
        stockingDao.insertAll(listOf(duplicate))
        val result = stockingDao.getStockingsInRange(
            startDate = date.minusDays(1),
            endDate = date
        ).first()

        assertEquals(1, result.size)
        assertEquals(duplicate.species, result[0].species)
    }

    @Test
    fun `getMostRecentStockingDate returns null when no stockings exist`() = runTest {
        val result = stockingDao.getMostRecentStockingDate()
        assertEquals(null, result)
    }

    @Test
    fun `getMostRecentStockingDate returns most recent date`() = runTest {
        val today = LocalDate.now()
        val stockings = listOf(
            createStocking(1, today, "Lake A"),
            createStocking(2, today.minusDays(1), "Lake B"),
            createStocking(3, today.minusDays(7), "Lake C")
        )
        stockingDao.insertAll(stockings)

        val result = stockingDao.getMostRecentStockingDate()

        assertEquals(today, result)
    }

    @Test
    fun `getMostRecentStockingDate handles multiple stockings on same date`() = runTest {
        val today = LocalDate.now()
        val stockings = listOf(
            createStocking(1, today, "Lake A"),
            createStocking(2, today, "Lake B"),
            createStocking(3, today.minusDays(1), "Lake C")
        )
        stockingDao.insertAll(stockings)

        val result = stockingDao.getMostRecentStockingDate()

        assertEquals(today, result)
    }

    private fun createStocking(
        id: Int,
        date: LocalDate,
        waterbody: String,
        county: String = "Test County",
        category: String = "Category A",
        species: List<String> = listOf("Rainbow"),
        isNationalForest: Boolean = false
    ) = StockingEntity(
        id = id,
        date = date,
        county = county,
        waterbody = waterbody,
        category = category,
        species = species,
        isNationalForest = isNationalForest,
        lastUpdated = date.atStartOfDay()
    )
} 