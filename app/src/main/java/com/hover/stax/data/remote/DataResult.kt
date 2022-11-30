package com.hover.stax.data.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

sealed interface DataResult<out T> {
    object Loading : DataResult<Nothing>
    data class Success<T>(val data: T) : DataResult<T>
    data class Error(val exception: Throwable? = null) : DataResult<Nothing>
}

fun <T> Flow<T>.asDataResult(): Flow<DataResult<T>> {
    return this
        .map<T, DataResult<T>> {
            DataResult.Success(it)
        }
        .onStart { emit(DataResult.Loading) }
        .catch { emit(DataResult.Error(it)) }
}