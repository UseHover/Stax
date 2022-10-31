package com.hover.stax.data.remote

sealed interface DataResult<out T : Any> {
    data class Loading<out T : Any>(val data: T?) : DataResult<T>
    data class Success<out T : Any>(val data: T) : DataResult<T>
    data class Error(val error: Throwable? = null) : DataResult<Nothing>
    object Empty : DataResult<Nothing>
}