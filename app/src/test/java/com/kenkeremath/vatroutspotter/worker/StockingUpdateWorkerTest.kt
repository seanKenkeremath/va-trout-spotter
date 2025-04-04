package com.kenkeremath.vatroutspotter.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.kenkeremath.vatroutspotter.domain.usecase.FetchAndNotifyStockingsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StockingUpdateWorkerTest {
    private lateinit var context: Context
    private lateinit var fetchAndNotifyStockingsUseCase: FetchAndNotifyStockingsUseCase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        fetchAndNotifyStockingsUseCase = mockk()
    }

    @Test
    fun `worker succeeds when use case succeeds`() = runTest {
        coEvery { fetchAndNotifyStockingsUseCase.execute() } returns Result.success(mockk())
        
        val worker = TestListenableWorkerBuilder<StockingUpdateWorker>(context)
            .setWorkerFactory(TestWorkerFactory(fetchAndNotifyStockingsUseCase))
            .build()
        val result = worker.doWork()
        
        coVerify { fetchAndNotifyStockingsUseCase.execute() }
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `worker returns retry when use case fails`() = runTest {
        coEvery { fetchAndNotifyStockingsUseCase.execute() } returns Result.failure(Exception("Test error"))
        
        val worker = TestListenableWorkerBuilder<StockingUpdateWorker>(context)
            .setWorkerFactory(TestWorkerFactory(fetchAndNotifyStockingsUseCase))
            .build()
        val result = worker.doWork()
        
        assertEquals(ListenableWorker.Result.retry(), result)
    }
}

class TestWorkerFactory(private val fetchAndNotifyStockingsUseCase: FetchAndNotifyStockingsUseCase) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            StockingUpdateWorker::class.java.name ->
                StockingUpdateWorker(appContext, workerParameters, fetchAndNotifyStockingsUseCase)
            else -> null
        }
    }
} 