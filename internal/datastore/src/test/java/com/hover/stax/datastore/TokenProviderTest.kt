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

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class TokenProviderTest {

    private var datastore = mockk<DataStore<Preferences>>(relaxed = true)
    private lateinit var testSubject: DefaultTokenProvider

    @Before
    fun setup() {
        testSubject = DefaultTokenProvider(datastore)
    }

    @Test
    fun `fetch returns the value stored in the data store`() = runBlocking {
        val expectedToken = "my_token"
        val preferences = mockk<Preferences>(relaxed = true)
        coEvery { datastore.data } returns flowOf(preferences)
        coEvery { preferences[DefaultTokenProvider.ACCESS_TOKEN] } returns expectedToken
        val actualToken = testSubject.fetch(DefaultTokenProvider.ACCESS_TOKEN).first()
        assertThat(expectedToken).isEqualTo(actualToken)
    }

    @Test
    fun `fetch returns null if there is no value stored in the data store`() = runBlocking {
        val key = DefaultTokenProvider.ACCESS_TOKEN
        every { datastore.data } returns flowOf(emptyPreferences())
        val result = testSubject.fetch(key).single()
        assertThat(result).isNull()
    }

    @Test
    fun `update stores the value in the data store`() = runBlocking {
        val token = "dummy_token"
        val preferences = mockk<Preferences>(relaxed = true)
        val key = DefaultTokenProvider.ACCESS_TOKEN
        coEvery { datastore.data } returns flowOf(preferences)
        coEvery { preferences[key] } returns token
        testSubject.update(key, token)
        assertThat(token).isEqualTo(preferences[key])
    }
}