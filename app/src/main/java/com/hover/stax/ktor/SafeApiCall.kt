package com.hover.stax.ktor

import com.hover.stax.data.remote.DataResult
import io.ktor.client.network.sockets.*

class ServerError(cause: Throwable) : Exception(cause)
class NetworkError : Exception()
class GenericError : Exception()

suspend fun <T : Any> dataResultSafeApiCall(
    apiCall: suspend () -> T
): DataResult<T> = try {
    DataResult.Success(apiCall.invoke())
} catch (throwable: Throwable) {
    when (throwable) {
        is ServerError -> {
            DataResult.Error(ServerError(throwable))
        }
        is ConnectTimeoutException, is NetworkError -> {
            DataResult.Error(NetworkError())
        }
        else -> {
            DataResult.Error(GenericError())
        }
    }
}