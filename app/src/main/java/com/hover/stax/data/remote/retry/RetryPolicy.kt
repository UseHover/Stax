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
