package com.seank.vatroutbuddy.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.seank.vatroutbuddy.data.repository.StockingRepository
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
    private lateinit var repository: StockingRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = mockk()
    }

    @Test
    fun `worker succeeds when refresh succeeds`() = runTest {
        coEvery { repository.fetchLatestStockings() } returns Result.success(mockk())
        
        val worker = TestListenableWorkerBuilder<StockingUpdateWorker>(context)
            .setWorkerFactory(TestWorkerFactory(repository))
            .build()
        val result = worker.doWork()
        
        coVerify { repository.fetchLatestStockings() }
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `worker returns retry when refresh fails`() = runTest {
        coEvery { repository.fetchLatestStockings() } returns Result.failure(Exception("Test error"))
        
        val worker = TestListenableWorkerBuilder<StockingUpdateWorker>(context)
            .setWorkerFactory(TestWorkerFactory(repository))
            .build()
        val result = worker.doWork()
        
        assertEquals(ListenableWorker.Result.retry(), result)
    }
}

class TestWorkerFactory(private val repository: StockingRepository) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            StockingUpdateWorker::class.java.name ->
                StockingUpdateWorker(appContext, workerParameters, repository)
            else -> null
        }
    }
} 