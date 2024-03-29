/*
 * Copyright 2023 Stax
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
package com.hover.stax.datastore

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LocalPreferencesTest {

    private lateinit var preferences: LocalPreferences

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        preferences = DefaultSharedPreferences(context)
    }

    @Test
    fun `WHEN getting a saved string THEN it should return the same string that was saved `() = runTest {
        val value = "test"
        preferences.putString(LocalPreferences.ENVIRONMENT, value)
        assertThat(value).isEqualTo(preferences.getString(LocalPreferences.ENVIRONMENT))
    }

    @Test
    fun `WHEN trying to retrieve non existent key THEN default value is returned`() = runTest {
        assertThat(preferences.getString("unknown_key")).isNull()
        assertThat("default").isEqualTo(preferences.getString("unknown_key", "default"))
    }

    @Test
    fun `WHEN putting a string by key THEN it should retrieve it using the same key`() = runTest {
        val value = "test"
        preferences.putString(LocalPreferences.ENVIRONMENT, value)
        assertThat(value).isEqualTo(preferences.getString(LocalPreferences.ENVIRONMENT))
    }

    @Test
    fun `WHEN clearing preferences THEN all previously saved key-value pairs should be removes`() = runTest {
        preferences.putString(LocalPreferences.ENVIRONMENT, "test")
        preferences.clear()
        assertThat(preferences.getString(LocalPreferences.ENVIRONMENT)).isNull()
    }
}