package com.seank.vatroutbuddy.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.seank.vatroutbuddy.data.repository.StockingRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.Month

@HiltWorker
class HistoricalDataWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: StockingRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            repository.fetchHistoricalData()
                .fold(
                    onSuccess = {
                        Result.success()
                    },
                    onFailure = { Result.retry() }
                )
        } catch (e: Exception) {
            Result.retry()
        }
    }
} 