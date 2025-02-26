package com.seank.vatroutbuddy.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.seank.vatroutbuddy.data.repository.StockingRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class StockingUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val stockingRepository: StockingRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return stockingRepository.refreshStockings()
            .fold(
                onSuccess = { Result.success() },
                onFailure = { Result.retry() }
            )
    }
} 