package com.kenkeremath.vatroutspotter.ui.features.stockings

import StockingFilters
import com.kenkeremath.vatroutspotter.AppConfig
import com.kenkeremath.vatroutspotter.data.repository.StockingRepository
import com.kenkeremath.vatroutspotter.domain.model.StockingInfo
import com.kenkeremath.vatroutspotter.domain.model.StockingsListPage
import com.kenkeremath.vatroutspotter.domain.usecase.FetchAndNotifyStockingsUseCase
import com.kenkeremath.vatroutspotter.util.Clock
import com.kenkeremath.vatroutspotter.util.TestFactory
import com.kenkeremath.vatroutspotter.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@ExperimentalCoroutinesApi
class StockingsViewModelTest {

    private lateinit var viewModel: StockingsViewModel
    private lateinit var repository: StockingRepository
    private lateinit var fetchAndNotifyStockingsUseCase: FetchAndNotifyStockingsUseCase
    private lateinit var clock: Clock
    private val testDispatcher = StandardTestDispatcher()
    private val hasInitialDataFlow = MutableStateFlow(false)
    private val hasHistoricalDataFlow = MutableStateFlow(false)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        fetchAndNotifyStockingsUseCase = mockk(relaxed = true)
        clock = mockk()
        coEvery { repository.hasHistoricalData } returns hasHistoricalDataFlow
        coEvery { repository.hasInitialData } returns hasInitialDataFlow
        
        // Setup default responses
        coEvery { repository.loadSavedStockings(any(), any()) } returns Result.success(
            StockingsListPage(emptyList(), false)
        )
        coEvery { fetchAndNotifyStockingsUseCase.execute() } returns Result.success(emptyList())
        coEvery { repository.getAllCounties() } returns emptyList()
        
        // Setup clock with initial time
        every { clock.currentTimeMillis() } returns 1000L
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load shows loading state and then success state`() = runTest {
        val mockStockings = createMockStockings(5)
        val mockPage = StockingsListPage(mockStockings, true)
        coEvery { fetchAndNotifyStockingsUseCase.execute() } returns Result.success(mockStockings)
        coEvery { repository.loadSavedStockings(pageSize = any(), stockingFilters = any()) } returns Result.success(mockPage)

        createViewModel()

        // Initial state should be Loading
        assertTrue(viewModel.uiState.value is HomeUiState.Uninitialized)

        // Advance time to complete loading
        advanceUntilIdle()

        // Final state should be Success
        val finalState = viewModel.uiState.value
        assertTrue(finalState is HomeUiState.Success)
        assertEquals(mockStockings.size, (finalState as HomeUiState.Success).stockings.size)
    }

    @Test
    fun `initial load error shows error state`() = runTest {
        coEvery { fetchAndNotifyStockingsUseCase.execute() } returns Result.failure(Exception())
        coEvery {
            repository.loadSavedStockings(
                pageSize = any(),
                stockingFilters = any()
            )
        } returns Result.failure(
            Exception()
        )

        createViewModel()

        // Initial state should be Loading
        assertTrue(viewModel.uiState.value is HomeUiState.Uninitialized)

        // Advance time to complete loading
        advanceUntilIdle()

        // Final state should be Error
        val finalState = viewModel.uiState.value
        assertTrue(finalState is HomeUiState.Error)
        assertEquals(R.string.generic_error_message, (finalState as HomeUiState.Error).messageResId)
    }

    @Test
    fun `isRefreshing is true during refresh with saved data and false after completion`() = runTest {
        val mockStockings = createMockStockings(5)
        val mockPage = StockingsListPage(mockStockings, true)

        // Set up repository to delay returning results
        coEvery { fetchAndNotifyStockingsUseCase.execute() } coAnswers {
            delay(500L)
            Result.success(mockStockings)
        }
        coEvery {
            repository.loadSavedStockings(
                pageSize = any(),
                stockingFilters = any()
            )
        } returns Result.success(mockPage)
        hasInitialDataFlow.value = true

        createViewModel()
        // Advance time to complete loading
        advanceUntilIdle()
        assertEquals(RefreshingState.Idle, viewModel.refreshingState.value)

        viewModel.refreshStockings()

        // isRefreshing should be true during refresh
        advanceTimeBy(250L)
        assertEquals(RefreshingState.Refreshing, viewModel.refreshingState.value)

        // Advance time to complete loading
        advanceUntilIdle()

        // isRefreshing should be false after completion
        assertEquals(RefreshingState.Idle, viewModel.refreshingState.value)
    }

    @Test
    fun `loadMoreStockings loads additional stockings successfully`() = runTest {
        val initialStockings = createMockStockings(5)
        val additionalStockings = createMockStockings(5, startId = 6)
        val initialPage = StockingsListPage(initialStockings, true)
        val additionalPage = StockingsListPage(additionalStockings, true)

        coEvery { fetchAndNotifyStockingsUseCase.execute() } returns Result.success(initialStockings)
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

        createViewModel()
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

        coEvery { fetchAndNotifyStockingsUseCase.execute() } returns Result.success(initialStockings)
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

        createViewModel()
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

        coEvery { fetchAndNotifyStockingsUseCase.execute() } returns Result.success(initialStockings)
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

        createViewModel()
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

            coEvery { fetchAndNotifyStockingsUseCase.execute() } returns Result.success(initialStockings)
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

            createViewModel()
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

        coEvery { fetchAndNotifyStockingsUseCase.execute() } returns Result.success(initialStockings)
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

        createViewModel()
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

    @Test
    fun `updating filters reloads stockings with new filters`() = runTest {
        val initialStockings = createMockStockings(5)
        val filteredStockings = createMockStockings(2, county = "Filtered County")
        val initialPage = StockingsListPage(initialStockings, true)
        val filteredPage = StockingsListPage(filteredStockings, false)

        coEvery { fetchAndNotifyStockingsUseCase.execute() } returns Result.success(initialStockings)
        coEvery { 
            repository.loadSavedStockings(
                pageSize = any(),
                stockingFilters = any()
            )
        } returns Result.success(initialPage)
        
        val newFilters = StockingFilters(counties = setOf("Filtered County"))
        coEvery { 
            repository.loadSavedStockings(
                pageSize = any(),
                stockingFilters = eq(newFilters)
            )
        } returns Result.success(filteredPage)

        createViewModel()
        advanceUntilIdle()

        // Verify initial state
        val initialState = viewModel.uiState.value
        assertTrue(initialState is HomeUiState.Success)
        assertEquals(5, (initialState as HomeUiState.Success).stockings.size)

        // Update filters
        viewModel.updateFilters(newFilters)
        advanceUntilIdle()

        // Verify filtered state
        val filteredState = viewModel.uiState.value
        assertTrue(filteredState is HomeUiState.Success)
        assertEquals(2, (filteredState as HomeUiState.Success).stockings.size)
        assertTrue((filteredState).stockings.all { it.county == "Filtered County" })
    }

    @Test
    fun `clearing filters resets to unfiltered state`() = runTest {
        hasHistoricalDataFlow.value = true
        val initialStockings = createMockStockings(5)
        val filteredStockings = createMockStockings(2, county = "Filtered County")
        val initialPage = StockingsListPage(initialStockings, true)
        val filteredPage = StockingsListPage(filteredStockings, false)

        coEvery { fetchAndNotifyStockingsUseCase.execute() } returns Result.success(initialStockings)
        coEvery { 
            repository.loadSavedStockings(
                pageSize = any(),
                stockingFilters = any()
            )
        } returns Result.success(initialPage)
        
        val filters = StockingFilters(counties = setOf("Filtered County"))
        coEvery { 
            repository.loadSavedStockings(
                pageSize = any(),
                stockingFilters = eq(filters)
            )
        } returns Result.success(filteredPage)

        createViewModel()
        advanceUntilIdle()

        // Apply filters
        viewModel.updateFilters(filters)
        advanceUntilIdle()

        // Verify filtered state
        val filteredState = viewModel.uiState.value
        assertTrue(filteredState is HomeUiState.Success)
        assertEquals(2, (filteredState as HomeUiState.Success).stockings.size)

        // Clear filters
        viewModel.clearFilters()
        advanceUntilIdle()

        // Verify unfiltered state
        val unfilteredState = viewModel.uiState.value
        assertTrue(unfilteredState is HomeUiState.Success)
        assertEquals(5, (unfilteredState as HomeUiState.Success).stockings.size)
    }

    @Test
    fun `loading more stockings preserves active filters`() = runTest {
        hasHistoricalDataFlow.value = true
        val initialStockings = createMockStockings(5, county = "Filtered County")
        val moreStockings = createMockStockings(3, county = "Filtered County", startId = 6)
        val initialPage = StockingsListPage(initialStockings, true)
        val nextPage = StockingsListPage(moreStockings, false)

        val filters = StockingFilters(counties = setOf("Filtered County"))
        
        coEvery { fetchAndNotifyStockingsUseCase.execute() } returns Result.success(initialStockings)
        coEvery { 
            repository.loadSavedStockings(
                pageSize = any(),
                stockingFilters = eq(filters)
            )
        } returns Result.success(initialPage)
        
        coEvery {
            repository.loadMoreSavedStockings(
                lastDate = any(),
                lastWaterbody = any(),
                lastId = any(),
                pageSize = any(),
                stockingFilters = eq(filters)
            )
        } returns Result.success(nextPage)

        createViewModel()
        viewModel.updateFilters(filters)
        advanceUntilIdle()

        // Verify initial filtered state
        val initialState = viewModel.uiState.value as HomeUiState.Success
        assertEquals(5, initialState.stockings.size)
        assertTrue(initialState.stockings.all { it.county == "Filtered County" })

        // Load more
        viewModel.loadMoreStockings()
        advanceUntilIdle()

        // Verify that new items also respect filters
        val updatedState = viewModel.uiState.value as HomeUiState.Success
        assertEquals(8, updatedState.stockings.size)
        assertTrue(updatedState.stockings.all { it.county == "Filtered County" })
    }

    @Test
    fun `refreshStockings respects throttle time`() = runTest {
        every { clock.currentTimeMillis() } returns 1000L
        
        viewModel = StockingsViewModel(
            repository,
            fetchAndNotifyStockingsUseCase,
            testDispatcher,
            clock
        )
        
        // Initial refresh should work
        viewModel.refreshStockings()
        advanceUntilIdle()
        
        // Verify the fetch was called
        coVerify(exactly = 1) { fetchAndNotifyStockingsUseCase.execute() }
        
        // Set current time to be within throttle window
        every { clock.currentTimeMillis() } returns 2000L  // 1 second later
        
        viewModel.refreshStockings()
        advanceUntilIdle()
        
        // Verify fetch was not called again
        coVerify(exactly = 1) { fetchAndNotifyStockingsUseCase.execute() }
        
        // set time beyond throttle window
        every { clock.currentTimeMillis() } returns 1000L + AppConfig.REFRESH_THROTTLE_MILLIS + 1000L
        
        // This should work
        viewModel.refreshStockings()
        advanceUntilIdle()
        
        // Verify fetch was called again
        coVerify(exactly = 2) { fetchAndNotifyStockingsUseCase.execute() }
    }

    private fun createViewModel() {
        viewModel = StockingsViewModel(repository, fetchAndNotifyStockingsUseCase, testDispatcher, clock)
    }

    private fun createMockStockings(count: Int, startId: Int = 1, county: String? = null): List<StockingInfo> {
        return (startId until startId + count).map { id ->
            TestFactory.createStockingInfo(
                id = id.toLong(),
                date = LocalDate.now().minusDays(id.toLong()),
                waterbody = "Lake $id",
                species = listOf("Rainbow Trout"),
                county = county ?: "County $id",
                category = "A",
                isNationalForest = false,
            )
        }
    }
} 