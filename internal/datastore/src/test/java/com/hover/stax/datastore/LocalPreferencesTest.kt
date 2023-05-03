package com.hover.stax.datastore

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.hover.stax.internal.datastore.DefaultSharedPreferences
import com.hover.stax.internal.datastore.LocalPreferences
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultSharedPreferencesTest {

    private lateinit var preferences: LocalPreferences

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        preferences = DefaultSharedPreferences(context)
    }
    @Test
    fun getString() {
        val value = "test"
        preferences.putString(LocalPreferences.ENVIRONMENT, value)
        assertThat(value).isEqualTo(preferences.getString(LocalPreferences.ENVIRONMENT))
    }


    @Test
    fun getStringDefault() {
        assertThat( preferences.getString("unknown_key")).isNull()
        assertThat("default").isEqualTo(preferences.getString("unknown_key", "default"))
    }

    @Test
    fun putString() {
        val value = "test"
        preferences.putString(LocalPreferences.ENVIRONMENT, value)
        assertThat(value).isEqualTo(preferences.getString(LocalPreferences.ENVIRONMENT))
    }

    @Test
    fun clear() {
        preferences.putString(LocalPreferences.ENVIRONMENT, "test")
        preferences.clear()
        assertThat( preferences.getString(LocalPreferences.ENVIRONMENT)).isNull()
    }
}