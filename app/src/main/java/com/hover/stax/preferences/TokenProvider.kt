package com.hover.stax.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

interface TokenProvider {

    fun fetch(key: Preferences.Key<String>): Flow<String?>

    suspend fun update(key: Preferences.Key<String>, token: String)
}

class DefaultTokenProvider(
    private val dataStore: DataStore<Preferences>
) : TokenProvider {

    override fun fetch(key: Preferences.Key<String>): Flow<String?> = dataStore.data
        .catch {
            emit(emptyPreferences())
        }.map { preferences ->
            preferences[key]
        }

    override suspend fun update(key: Preferences.Key<String>, token: String) {
        dataStore.edit { preferences ->
            preferences[key] = token
        }
    }

    companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }
}