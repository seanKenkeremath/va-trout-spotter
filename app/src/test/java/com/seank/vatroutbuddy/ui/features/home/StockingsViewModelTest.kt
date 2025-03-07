package com.seank.vatroutbuddy.ui.features.home

import com.seank.vatroutbuddy.data.repository.StockingRepository
import com.seank.vatroutbuddy.domain.model.StockingInfo
import com.seank.vatroutbuddy.domain.model.StockingsListPage
import com.seank.vatroutbuddy.util.TestFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@ExperimentalCoroutinesApi
class StockingsViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var repository: StockingRepository
    private val testDispatcher = StandardTestDispatcher()
    private val hasHistoricalDataFlow = MutableStateFlow(false)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        coEvery { repository.hasHistoricalData } returns hasHistoricalDataFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load shows loading state and then success state`() = runTest {
        val mockStockings = createMockStockings(5)
        val mockPage = StockingsListPage(mockStockings, true)
        coEvery { repository.fetchLatestStockings() } returns Result.success(mockStockings)
        coEvery { repository.loadSavedStockings(pageSize = any(), stockingFilters = any()) } returns Result.success(mockPage)

        viewModel = HomeViewModel(repository)

        // Initial state should be Loading
        assertTrue(viewModel.uiState.value is HomeUiState.Loading)

        // Advance time to complete loading
        advanceUntilIdle()

        // Final state should be Success
        val finalState = viewModel.uiState.value
        assertTrue(finalState is HomeUiState.Success)
        assertEquals(mockStockings.size, (finalState as HomeUiState.Success).stockings.size)
    }

    @Test
    fun `initial load error shows error state`() = runTest {
        val errorMessage = "Network error"
        coEvery { repository.fetchLatestStockings() } returns Result.failure(Exception(errorMessage))
        coEvery {
            repository.loadSavedStockings(
                pageSize = any(),
                stockingFilters = any()
            )
        } returns Result.failure(
            Exception(
                errorMessage
            )
        )

        viewModel = HomeViewModel(repository)

        // Initial state should be Loading
        assertTrue(viewModel.uiState.value is HomeUiState.Loading)

        // Advance time to complete loading
        advanceUntilIdle()

        // Final state should be Error
        val finalState = viewModel.uiState.value
        assertTrue(finalState is HomeUiState.Error)
        assertEquals(errorMessage, (finalState as HomeUiState.Error).message)
    }

    @Test
    fun `isRefreshing is true during fetch and false after completion`() = runTest {
        val mockStockings = createMockStockings(5)
        val mockPage = StockingsListPage(mockStockings, true)

        // Set up repository to delay returning results
        coEvery { repository.fetchLatestStockings() } returns Result.success(mockStockings)
        coEvery {
            repository.loadSavedStockings(
                pageSize = any(),
                stockingFilters = any()
            )
        } returns Result.success(mockPage)

        viewModel = HomeViewModel(repository)

        // isRefreshing should be true initially
        assertTrue(viewModel.isRefreshing.value)

        // Advance time to complete loading
        advanceUntilIdle()

        // isRefreshing should be false after completion
        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun `loadMoreStockings loads additional stockings successfully`() = runTest {
        val initialStockings = createMockStockings(5)
        val additionalStockings = createMockStockings(5, startId = 6)
        val initialPage = StockingsListPage(initialStockings, true)
        val additionalPage = StockingsListPage(additionalStockings, true)

        coEvery { repository.fetchLatestStockings() } returns Result.success(initialStockings)
        coEvery {
            repository.loadSavedStockings(
                pageSize = any(),
                stockingFilters = any()
            )
        } returns Result.success(initialPage)
        coEvery {
            repository.loadMoreSavedStockings(
                lastDate = any(),
                lastWaterbody = any(),
                lastId = any(),
                pageSize = any(),
                stockingFilters = any()
            )
        } returns Result.success(additionalPage)

        viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        // Verify initial state
        val initialState = viewModel.uiState.value
        assertTrue(initialState is HomeUiState.Success)
        assertEquals(initialStockings.size, (initialState as HomeUiState.Success).stockings.size)

        // Load more stockings
        viewModel.loadMoreStockings()
        advanceUntilIdle()

        // Verify updated state
        val updatedState = viewModel.uiState.value
        assertTrue(updatedState is HomeUiState.Success)
        assertEquals(
            initialStockings.size + additionalStockings.size,
            (updatedState as HomeUiState.Success).stockings.size
        )
    }

    @Test
    fun `loadMoreStockings shows error state when loading fails`() = runTest {
        val initialStockings = createMockStockings(5)
        val initialPage = StockingsListPage(initialStockings, true)
        val errorMessage = "Failed to load more stockings"

        coEvery { repository.fetchLatestStockings() } returns Result.success(initialStockings)
        coEvery {
            repository.loadSavedStockings(
                pageSize = any(),
                stockingFilters = any()
            )
        } returns Result.success(initialPage)
        coEvery {
            repository.loadMoreSavedStockings(
                lastDate = any(),
                lastWaterbody = any(),
                lastId = any(),
                pageSize = any(),
                stockingFilters = any()
            )
        } returns Result.failure(Exception(errorMessage))

        viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        // Verify initial paging state
        assertEquals(PagingState.Idle, viewModel.pagingState.value)

        // Load more stockings
        viewModel.loadMoreStockings()

        // Verify loading state
        assertEquals(PagingState.Loading, viewModel.pagingState.value)

        advanceUntilIdle()

        // Verify error state
        assertTrue(viewModel.pagingState.value is PagingState.Error)
    }

    @Test
    fun `loadMoreStockings fetches historical data when reaching end and none exists`() = runTest {
        val initialStockings = createMockStockings(5)
        val initialPage = StockingsListPage(initialStockings, true)
        val emptyPage = StockingsListPage(emptyList(), false)
        val historicalStockings = createMockStockings(3, startId = 100)

        coEvery { repository.fetchLatestStockings() } returns Result.success(initialStockings)
        coEvery {
            repository.loadSavedStockings(
                pageSize = any(),
                stockingFilters = any()
            )
        } returns Result.success(initialPage)
        coEvery {
            repository.loadMoreSavedStockings(
                lastDate = any(),
                lastWaterbody = any(),
                lastId = any(),
                pageSize = any(),
                stockingFilters = any()
            )
        } returns Result.success(emptyPage)
        coEvery { repository.fetchHistoricalData() } returns Result.success(historicalStockings)
        hasHistoricalDataFlow.value = false

        viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        // Verify initial paging state
        assertEquals(PagingState.Idle, viewModel.pagingState.value)

        // Load more stockings
        viewModel.loadMoreStockings()

        // Verify loading state
        assertEquals(PagingState.Loading, viewModel.pagingState.value)

        advanceUntilIdle()

        // Verify we go back to idle after fetching historical data
        assertEquals(PagingState.Idle, viewModel.pagingState.value)

        // Verify historical data was fetched
        coVerify { repository.fetchHistoricalData() }
    }

    @Test
    fun `loadMoreStockings shows reached end when no more data and historical data exists`() =
        runTest {
            val initialStockings = createMockStockings(5)
            val initialPage = StockingsListPage(initialStockings, true)
            val emptyPage = StockingsListPage(emptyList(), false)

            coEvery { repository.fetchLatestStockings() } returns Result.success(initialStockings)
            coEvery {
                repository.loadSavedStockings(
                    pageSize = any(),
                    stockingFilters = any()
                )
            } returns Result.success(initialPage)
            coEvery {
                repository.loadMoreSavedStockings(
                    lastDate = any(),
                    lastWaterbody = any(),
                    lastId = any(),
                    pageSize = any(),
                    stockingFilters = any()
                )
            } returns Result.success(emptyPage)
            hasHistoricalDataFlow.value = true

            viewModel = HomeViewModel(repository)
            advanceUntilIdle()

            // Verify initial paging state
            assertEquals(PagingState.Idle, viewModel.pagingState.value)

            // Load more stockings
            viewModel.loadMoreStockings()

            // Verify loading state
            assertEquals(PagingState.Loading, viewModel.pagingState.value)

            advanceUntilIdle()

            // Verify reached end state
            assertEquals(PagingState.ReachedEnd, viewModel.pagingState.value)
        }

    @Test
    fun `loadMoreStockings shows reached end when historical data fetch fails`() = runTest {
        val initialStockings = createMockStockings(5)
        val initialPage = StockingsListPage(initialStockings, true)
        val emptyPage = StockingsListPage(emptyList(), false)

        coEvery { repository.fetchLatestStockings() } returns Result.success(initialStockings)
        coEvery {
            repository.loadSavedStockings(
                pageSize = any(),
                stockingFilters = any()
            )
        } returns Result.success(initialPage)
        coEvery {
            repository.loadMoreSavedStockings(
                lastDate = any(),
                lastWaterbody = any(),
                lastId = any(),
                pageSize = any(),
                stockingFilters = any()
            )
        } returns Result.success(emptyPage)
        hasHistoricalDataFlow.value = false
        coEvery { repository.fetchHistoricalData() } returns Result.failure(Exception("Failed to fetch historical data"))

        viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        // Verify initial paging state
        assertEquals(PagingState.Idle, viewModel.pagingState.value)

        // Load more stockings
        viewModel.loadMoreStockings()

        // Verify loading state
        assertEquals(PagingState.Loading, viewModel.pagingState.value)

        advanceUntilIdle()

        // Verify reached end state after historical data fetch fails
        assertEquals(PagingState.ReachedEnd, viewModel.pagingState.value)
    }

    private fun createMockStockings(count: Int, startId: Int = 1): List<StockingInfo> {
        return (startId until startId + count).map { id ->
            TestFactory.createStockingInfo(
                id = id.toLong(),
                date = LocalDate.now().minusDays(id.toLong()),
                waterbody = "Lake $id",
                species = listOf("Rainbow Trout"),
                county = "County $id",
                category = "A",
                isNationalForest = false,
            )
        }
    }
} 