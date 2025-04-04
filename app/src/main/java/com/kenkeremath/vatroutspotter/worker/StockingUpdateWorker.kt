package com.kenkeremath.vatroutspotter.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kenkeremath.vatroutspotter.domain.usecase.FetchAndNotifyStockingsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class StockingUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val fetchAndNotifyStockingsUseCase: FetchAndNotifyStockingsUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            fetchAndNotifyStockingsUseCase.execute()
                .fold(
                    onSuccess = { Result.success() },
                    onFailure = { Result.retry() }
                )
        } catch (e: Exception) {
            Result.retry()
        }
    }
} 