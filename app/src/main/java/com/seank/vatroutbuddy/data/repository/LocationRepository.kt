package com.seank.vatroutbuddy.data.repository

import com.seank.vatroutbuddy.data.model.Location
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor() {
    fun getLocations(): Flow<List<Location>> {
        // TODO: Implement actual data source
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }
} 