package com.seank.vatroutbuddy.ui.features.detail

import androidx.lifecycle.SavedStateHandle
import com.seank.vatroutbuddy.data.repository.StockingRepository
import com.seank.vatroutbuddy.domain.model.StockingInfo
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
class StockingDetailViewModelTest {

    private lateinit var viewModel: StockingDetailViewModel
    private lateinit var repository: StockingRepository
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testStocking: StockingInfo

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        testStocking = createMockStocking(123L)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {

        // Setup repository responses
        val relatedStockings = listOf(
            createMockStocking(456L, "Lake Test"),
            createMockStocking(789L, "Lake Test")
        )
        
        coEvery { repository.getStockingsByWaterbody("Lake Test", any(), testStocking.id) } returns 
            Result.success(relatedStockings)
        
        viewModel = StockingDetailViewModel(testStocking, repository)
        
        // Initial state should be Loading
        assertTrue(viewModel.uiState.value is StockingDetailUiState.Loading)
    }
    
    @Test
    fun `loads related stockings successfully`() = runTest {
        // Setup repository responses
        val relatedStockings = listOf(
            createMockStocking(456L, "Lake Test"),
            createMockStocking(789L, "Lake Test")
        )
        
        coEvery { repository.getStockingsByWaterbody("Lake Test", any(), testStocking.id) } returns 
            Result.success(relatedStockings)
        
        viewModel = StockingDetailViewModel(testStocking, repository)
        
        // Advance time to complete loading
        advanceUntilIdle()
        
        // Final state should be Success
        val finalState = viewModel.uiState.value
        assertTrue(finalState is StockingDetailUiState.Success)
        
        val successState = finalState as StockingDetailUiState.Success
        assertEquals(testStocking, successState.stocking)
        assertEquals(relatedStockings, successState.relatedStockings)
    }
    
    @Test
    fun `shows error state when loading fails`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("stocking" to testStocking))
        val errorMessage = "Stocking not found"
        
        coEvery { repository.getStockingsByWaterbody("Lake Test", any(), testStocking.id) } returns 
            Result.failure(Exception(errorMessage))
        
        viewModel = StockingDetailViewModel(testStocking, repository)
        
        // Advance time to complete loading
        advanceUntilIdle()
        
        // Final state should be Error
        val finalState = viewModel.uiState.value
        assertTrue(finalState is StockingDetailUiState.Error)
        assertEquals(errorMessage, (finalState as StockingDetailUiState.Error).message)
    }
    
    @Test
    fun `retry reloads stocking details`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("stocking" to testStocking))
        val errorMessage = "Network error"
        
        // First attempt fails
        coEvery { repository.getStockingsByWaterbody("Lake Test", any(), testStocking.id) } returns 
            Result.failure(Exception(errorMessage))
        
        viewModel = StockingDetailViewModel(testStocking, repository)
        advanceUntilIdle()
        
        // Verify error state
        assertTrue(viewModel.uiState.value is StockingDetailUiState.Error)
        
        // Setup success for retry
        val relatedStockings = listOf(createMockStocking(456L, "Lake Test"))
        
        coEvery { repository.getStockingsByWaterbody("Lake Test", any(), testStocking.id) } returns 
            Result.success(relatedStockings)
        
        // Retry
        viewModel.retry()
        advanceUntilIdle()
        
        // Verify success state
        val finalState = viewModel.uiState.value
        assertTrue(finalState is StockingDetailUiState.Success)
    }
    
    private fun createMockStocking(id: Long, waterbody: String = "Lake Test"): StockingInfo {
        return StockingInfo(
            id = id,
            date = LocalDate.now().minusDays(id % 10),
            waterbody = waterbody,
            species = listOf("Rainbow Trout"),
            county = "Test County",
            category = "A",
            isNationalForest = false
        )
    }
} 