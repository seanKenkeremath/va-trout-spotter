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
    fun `getStockingsAfter returns only stockings after specified date`() = runTest {
        val today = LocalDate.now()
        val stockings = listOf(
            createStocking(1, today, "Lake A"),
            createStocking(2, today.minusDays(1), "Lake B"),
            createStocking(3, today.minusDays(7), "Lake C"),
            createStocking(4, today.minusDays(30), "Lake D")
        )
        stockingDao.insertAll(stockings)

        val result = stockingDao.getStockingsAfter(today.minusDays(7)).first()

        assertEquals(3, result.size)
        assertEquals(setOf("Lake A", "Lake B", "Lake C"), result.map { it.waterbody }.toSet())
    }

    @Test
    fun `insertAll replaces duplicates based on waterbody and date`() = runTest {
        val date = LocalDate.now()
        val original = createStocking(1, date, "Lake A", species = listOf("Rainbow"))
        val duplicate = createStocking(2, date, "Lake A", species = listOf("Brook", "Brown"))
        
        stockingDao.insertAll(listOf(original))
        stockingDao.insertAll(listOf(duplicate))
        val result = stockingDao.getStockingsAfter(date.minusDays(1)).first()

        assertEquals(1, result.size)
        assertEquals(duplicate.species, result[0].species)
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