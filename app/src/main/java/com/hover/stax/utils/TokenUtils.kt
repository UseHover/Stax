package com.hover.stax.utils

import android.util.Base64
import com.hover.stax.data.remote.dto.StaxUserDto
import com.hover.stax.data.remote.dto.authorization.TokenData
import com.hover.stax.data.remote.dto.toStaxUser
import com.hover.stax.domain.model.StaxUser
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.UnsupportedEncodingException

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