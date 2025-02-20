package com.seank.vatroutbuddy.data.repository

import com.seank.vatroutbuddy.data.model.StockingInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockingRepository @Inject constructor() {
    fun getRecentStockings(): Flow<List<StockingInfo>> {
        // TODO: Implement actual data source
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }
} 