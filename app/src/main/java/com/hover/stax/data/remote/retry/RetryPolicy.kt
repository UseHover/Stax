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
package com.hover.stax.data.remote.retry

/**
 * Retry policy with exponential backoff.
 *
 * delayFactor is used to multiply delayMillis to increase the delay for the next retry.
 *
 * For instance, given a policy with numRetries of 4, delayMillis of 400ms and delayFactor of 2:
 *  - first retry: effective delayMillis will be 400
 *  - second retry: effective delayMillis will be 800
 *  - third retry: effective delayMillis will be 1600
 *  - forth retry: effective delayMillis will be 3200
 *
 * If no exponential backoff is desired, set delayFactor to 1
 */
interface RetryPolicy {
    val numRetries: Long
    val delayMillis: Long
    val delayFactor: Long
}

data class DefaultRetryPolicy(
    override val numRetries: Long = 4,
    override val delayMillis: Long = 400,
    override val delayFactor: Long = 2
) : RetryPolicy