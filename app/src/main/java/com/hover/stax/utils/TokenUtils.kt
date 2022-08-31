package com.hover.stax.utils


import android.util.Base64
import com.google.gson.Gson
import com.hover.stax.data.remote.dto.StaxUserDto
import com.hover.stax.data.remote.dto.authorization.TokenData
import com.hover.stax.data.remote.dto.toStaxUser
import com.hover.stax.domain.model.StaxUser
import timber.log.Timber
import java.io.UnsupportedEncodingException

object TokenUtils {

    @Throws(Exception::class)
    fun decodeToken(token: String): StaxUser? {
        return try {
            val split = token.split(".")

            val gson = Gson()
            val tokenData = gson.fromJson(getJson(split[1]), TokenData::class.java)
            val userDto = gson.fromJson(tokenData.user, StaxUserDto::class.java)

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