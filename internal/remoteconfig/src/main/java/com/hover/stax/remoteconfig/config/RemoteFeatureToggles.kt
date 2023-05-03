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
package com.hover.stax.remoteconfig.config

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.hover.stax.remoteconfig.R
import timber.log.Timber
import javax.inject.Inject

class RemoteFeatureToggles @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
    private val configConfig: RemoteConfigConfig
) {

    /**
     * Syncs our local configs with the server
     *
     * Fetch interval means, no matter how often this method is called, it'll only hit the server after N hours.
     */
    fun sync() {
        remoteConfig.apply {
            setDefaultsAsync(R.xml.remote_config_defaults)
            setConfigSettingsAsync(
                remoteConfigSettings {
                    minimumFetchIntervalInSeconds = configConfig.minimumFetchIntervalInSeconds()
                }
            )
            fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("Config params updated: ${task.result}")
                } else {
                    Timber.e("Config params update failed from remote.")
                }
            }
        }
    }

    fun getBoolean(key: String): Boolean = remoteConfig.getBoolean(key)

    fun getString(key: String): String = remoteConfig.getString(key)

    fun getDouble(key: String): Double = remoteConfig.getDouble(key)

    fun getLong(key: String): Long = remoteConfig.getLong(key)
}