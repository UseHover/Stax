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