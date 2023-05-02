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
package com.hover.stax.utils

import android.util.Base64
import com.hover.stax.data.remote.dto.StaxUserDto
import com.hover.stax.data.remote.dto.authorization.TokenData
import com.hover.stax.data.remote.dto.toStaxUser
import com.hover.stax.database.models.StaxUser
import java.io.UnsupportedEncodingException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber

object TokenUtils {

    @Throws(Exception::class)
    fun decodeToken(token: String): StaxUser? {
        return try {
            val split = token.split(".")

            val tokenData = Json.decodeFromString<TokenData>(split[1]) // can we use .last() ???
            val userDto = Json.decodeFromString<StaxUserDto>(tokenData.user)

            return userDto.toStaxUser()
        } catch (e: UnsupportedEncodingException) {
            Timber.e(e)
            null
        }
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getJson(encodedString: String): String {
        val decodedBytes = Base64.decode(encodedString, Base64.URL_SAFE)
        return String(decodedBytes, Charsets.UTF_8)
    }
}