package com.kenkeremath.vatroutspotter.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.kenkeremath.vatroutspotter.data.repository.StockingRepository
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
class HistoricalDataWorkerTest {
    private lateinit var context: Context
    private lateinit var repository: StockingRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = mockk()
    }

    @Test
    fun `worker succeeds when historical data fetch succeeds`() = runTest {
        coEvery { repository.fetchHistoricalData() } returns Result.success(mockk())
        
        val worker = TestListenableWorkerBuilder<HistoricalDataWorker>(context)
            .setWorkerFactory(TestWorkerFactory(repository))
            .build()
        val result = worker.doWork()
        
        coVerify { repository.fetchHistoricalData() }
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `worker returns retry when historical data fetch fails`() = runTest {
        coEvery { repository.fetchHistoricalData() } returns Result.failure(Exception("Test error"))
        
        val worker = TestListenableWorkerBuilder<HistoricalDataWorker>(context)
            .setWorkerFactory(TestWorkerFactory(repository))
            .build()
        val result = worker.doWork()
        
        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun `worker returns retry when historical data fetch throws exception`() = runTest {
        coEvery { repository.fetchHistoricalData() } throws Exception("Test error")
        
        val worker = TestListenableWorkerBuilder<HistoricalDataWorker>(context)
            .setWorkerFactory(TestWorkerFactory(repository))
            .build()
        val result = worker.doWork()
        
        assertEquals(ListenableWorker.Result.retry(), result)
    }

    private class TestWorkerFactory(
        private val repository: StockingRepository
    ) : androidx.work.WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker? {
            return when (workerClassName) {
                HistoricalDataWorker::class.java.name ->
                    HistoricalDataWorker(appContext, workerParameters, repository)
                else -> null
            }
        }
    }
} 