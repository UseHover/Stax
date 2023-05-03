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