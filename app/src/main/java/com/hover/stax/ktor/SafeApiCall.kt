package com.hover.stax.ktor

import com.hover.stax.data.remote.DataResult
import io.ktor.client.call.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*

class ServerError(cause: Throwable) : Exception(cause)
class NetworkError : Exception()
class GenericError : Exception()

suspend fun <T : Any> dataResultSafeApiCall(
    apiCall: suspend () -> T
): DataResult<T> = try {
    DataResult.Success(apiCall.invoke())
} catch (exception: Exception) {
    when (exception) {
        is ServerResponseException, is NoTransformationFoundException -> {
            DataResult.Error(ServerError(exception))
        }
        is ConnectTimeoutException, is NetworkError -> {
            DataResult.Error(NetworkError())
        }
        else -> {
            DataResult.Error(GenericError())
        }
    }
}