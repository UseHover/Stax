/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.internal.datastore

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