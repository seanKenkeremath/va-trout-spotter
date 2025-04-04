package com.kenkeremath.vatroutspotter.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val hasDownloadedInitialData: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[INITIAL_DATA_DOWNLOADED] ?: false
    }

    suspend fun setInitialDataDownloaded(downloaded: Boolean) {
        dataStore.edit { preferences ->
            preferences[INITIAL_DATA_DOWNLOADED] = downloaded
        }
    }

    val hasDownloadedHistoricalData: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[HISTORICAL_DATA_DOWNLOADED] ?: false
    }

    suspend fun setHistoricalDataDownloaded(downloaded: Boolean) {
        dataStore.edit { preferences ->
            preferences[HISTORICAL_DATA_DOWNLOADED] = downloaded
        }
    }

    companion object {
        private val HISTORICAL_DATA_DOWNLOADED = booleanPreferencesKey("historical_data_downloaded")
        private val INITIAL_DATA_DOWNLOADED = booleanPreferencesKey("initial_data_downloaded")
    }
} 