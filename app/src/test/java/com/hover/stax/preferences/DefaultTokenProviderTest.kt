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
package com.hover.stax.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.hover.stax.StaxApplication
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(application = StaxApplication::class)
@RunWith(RobolectricTestRunner::class)
class DefaultTokenProviderTest {

    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var tokenProvider: DefaultTokenProvider

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        testDataStore = PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("test") }
        )
        tokenProvider = DefaultTokenProvider(testDataStore)
    }

    @Test
    fun `test successful access token update and fetch`() {
        runBlocking {
            tokenProvider.update(stringPreferencesKey("access_token"), "some_random_token_here")
            val response = tokenProvider.fetch(stringPreferencesKey("access_token")).firstOrNull()
            assertThat(response).isEqualTo("some_random_token_here")
        }
    }

    @Test
    fun `test successful refresh token update and fetch`() {
        runBlocking {
            tokenProvider.update(stringPreferencesKey("refresh_token"), "some_random_token_here")
            val response = tokenProvider.fetch(stringPreferencesKey("refresh_token")).firstOrNull()
            assertThat(response).isEqualTo("some_random_token_here")
        }
    }
}