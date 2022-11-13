package com.hover.stax.preferences

import android.content.Context

interface LocalPreferences {

    companion object {
        const val ENVIRONMENT = "environment"
    }

    fun getString(key: String, default: String? = null): String?

    fun putString(key: String, value: String)

    fun clear()
}

class DefaultSharedPreferences(
    private val context: Context
) : LocalPreferences {

    companion object {
        private const val SHARED_PREFERENCES_NAME = "stax.preferences"
    }

    private val sharedPreferences by lazy {
        context.getSharedPreferences(
            SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
    }

    override fun getString(key: String, default: String?): String? =
        sharedPreferences.getString(key, default)

    override fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}