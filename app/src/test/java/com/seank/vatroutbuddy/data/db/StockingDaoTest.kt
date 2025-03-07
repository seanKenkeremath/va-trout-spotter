package com.seank.vatroutbuddy.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(RobolectricTestRunner::class)
class StockingDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var stockingDao: StockingDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        stockingDao = database.stockingDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `insertStockings inserts stockings and returns their ids`() = runBlocking {
        val stocking = createStocking(1, LocalDate.now(), "Lake A")
        val ids = stockingDao.insertStockings(listOf(stocking))
        assertEquals(1, ids.size)
    }

    @Test
    fun `getMostRecentStockings returns the most recent stockings`() = runBlocking {
        val today = LocalDate.now()
        val stockings = listOf(
            createStocking(1, today, "Lake A"),
            createStocking(2, today.minusDays(1), "Lake B"),
            createStocking(3, today.minusDays(2), "Lake C"),
            createStocking(4, today.minusDays(3), "Lake D"),
            createStocking(5, today.minusDays(4), "Lake E")
        )
        stockingDao.insertStockings(stockings)

        val result = stockingDao.getMostRecentStockings(limit = 2)
        assertEquals(2, result.size)
        assertEquals("Lake A", result[0].waterbody)
        assertEquals("Lake B", result[1].waterbody)
    }

    @Test
    fun `getMostRecentStockingDate returns null when no stockings exist`() = runBlocking {
        val result = stockingDao.getMostRecentStockingDate()
        assertEquals(null, result)
    }

    @Test
    fun `getMostRecentStockingDate returns most recent date`() = runBlocking {
        val today = LocalDate.now()
        val stockings = listOf(
            createStocking(1, today, "Lake A"),
            createStocking(2, today.minusDays(1), "Lake B"),
            createStocking(3, today.minusDays(7), "Lake C")
        )
        stockingDao.insertStockings(stockings)

        val result = stockingDao.getMostRecentStockingDate()
        assertEquals(today, result)
    }

    @Test
    fun `getStockingsById returns stockings by their ids`() = runBlocking {
        val stockings = listOf(
            createStocking(1, LocalDate.now(), "Lake A"),
            createStocking(2, LocalDate.now(), "Lake B"),
            createStocking(3, LocalDate.now().minusDays(1), "Lake C")
        )
        val ids = stockingDao.insertStockings(stockings)

        val result = stockingDao.getStockingsById(ids.take(2)) // Fetching only the first two entries
        assertEquals(2, result.size)
        assertEquals("Lake A", result[0].waterbody)
        assertEquals("Lake B", result[1].waterbody)
    }

    @Test
    fun `insertAndReturnStockings inserts and returns the inserted stockings`() = runBlocking {
        val stocking = createStocking(1, LocalDate.now(), "Lake A")
        val result = stockingDao.insertAndReturnStockings(listOf(stocking))

        assertEquals(1, result.size)
        assertEquals("Lake A", result[0].waterbody)
    }

    @Test
    fun `getStockingsPaged returns paged stockings based on criteria`() = runBlocking {
        val today = LocalDate.now()
        val stocking1 = createStocking(1, today, "Lake A")
        val stocking2 = createStocking(2, today, "Lake B")
        val stocking3 = createStocking(3, today.minusDays(1), "Lake C")
        stockingDao.insertStockings(listOf(stocking1, stocking2, stocking3))

        val result = stockingDao.getStockingsPaged(
            lastDate = today,
            lastWaterbody = "Lake A",
            lastId = 1,
            pageSize = 2
        )
        assertEquals(2, result.size)
        assertEquals("Lake B", result[0].waterbody)
        assertEquals("Lake C", result[1].waterbody)
    }

    @Test
    fun `getMostRecentStockings filters by counties`() = runBlocking {
        val stockings = listOf(
            createStocking(1, LocalDate.now(), "Lake A", county = "County A"),
            createStocking(2, LocalDate.now(), "Lake B", county = "County B"),
            createStocking(3, LocalDate.now(), "Lake C", county = "County A")
        )
        stockingDao.insertStockings(stockings)

        val result = stockingDao.getMostRecentStockings(
            counties = listOf("County A"),
            limit = 10
        )
        
        assertEquals(2, result.size)
        assertTrue(result.all { it.county == "County A" })
    }

    @Test
    fun `getMostRecentStockings filters by multiple counties`() = runBlocking {
        val stockings = listOf(
            createStocking(1, LocalDate.now(), "Lake A", county = "County A"),
            createStocking(2, LocalDate.now(), "Lake B", county = "County B"),
            createStocking(3, LocalDate.now(), "Lake C", county = "County C")
        )
        stockingDao.insertStockings(stockings)

        val result = stockingDao.getMostRecentStockings(
            counties = listOf("County A", "County C"),
            limit = 10
        )

        assertEquals(2, result.size)
        assertTrue(result.any { it.county == "County A" })
        assertTrue(result.any { it.county == "County C" })
    }

    @Test
    fun `getMostRecentStockings filters by multiple criteria`() = runBlocking {
        val stockings = listOf(
            createStocking(1, LocalDate.now(), "Lake A", isNationalForest = true, isNsf = true),
            createStocking(2, LocalDate.now(), "Lake B", isNationalForest = true, isNsf = false),
            createStocking(3, LocalDate.now(), "Lake C", isNationalForest = false, isNsf = false)
        )
        stockingDao.insertStockings(stockings)

        val result = stockingDao.getMostRecentStockings(
            isNationalForest = true,
            isNsf = true,
            limit = 10
        )
        
        assertEquals(1, result.size)
        assertTrue(result[0].isNationalForest)
        assertTrue(result[0].isNsf)
    }

    @Test
    fun `getStockingsPaged applies filters correctly`() = runBlocking {
        val today = LocalDate.now()
        val stockings = listOf(
            createStocking(1, today, "Lake A", county = "County A", isHeritageDayWater = true),
            createStocking(2, today, "Lake B", county = "County B", isHeritageDayWater = false),
            createStocking(3, today.minusDays(1), "Lake C", county = "County A", isHeritageDayWater = true)
        )
        stockingDao.insertStockings(stockings)

        val result = stockingDao.getStockingsPaged(
            lastDate = today,
            lastWaterbody = "Lake A",
            lastId = 1,
            counties = listOf("County A"),
            isHeritageDayWater = true,
            pageSize = 10
        )
        
        assertEquals(1, result.size)
        assertEquals("Lake C", result[0].waterbody)
        assertTrue(result[0].isHeritageDayWater)
        assertEquals("County A", result[0].county)
    }

    private fun createStocking(
        id: Int,
        date: LocalDate,
        waterbody: String,
        county: String = "Test County",
        category: String = "Category A",
        species: List<String> = listOf("Rainbow"),
        isNationalForest: Boolean = false,
        isNsf: Boolean = false,
        isHeritageDayWater: Boolean = false,
        isDelayedHarvest: Boolean = false,
    ) = StockingEntity(
        id = id.toLong(),
        date = date,
        county = county,
        waterbody = waterbody,
        category = category,
        species = species,
        isNationalForest = isNationalForest,
        isNsf = isNsf,
        isHeritageDayWater = isHeritageDayWater,
        isDelayedHarvest = isDelayedHarvest,
        lastUpdated = LocalDateTime.now()
    )
}