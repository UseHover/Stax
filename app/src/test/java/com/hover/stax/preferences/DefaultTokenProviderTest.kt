package com.hover.stax.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

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
            MatcherAssert.assertThat(response, CoreMatchers.`is`("some_random_token_here"))
        }
    }

    @Test
    fun `test successful refresh token update and fetch`() {
        runBlocking {
            tokenProvider.update(stringPreferencesKey("refresh_token"), "some_random_token_here")
            val response = tokenProvider.fetch(stringPreferencesKey("refresh_token")).firstOrNull()
            MatcherAssert.assertThat(response, CoreMatchers.`is`("some_random_token_here"))
        }
    }
}