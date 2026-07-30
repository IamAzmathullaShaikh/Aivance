package com.bangersoul.aivance.sdk.core

/**
 * Performance and usage metrics for an AI provider.
 *
 * @property requestCount Total number of requests made to this provider.
 * @property errorCount Number of requests that resulted in an error.
 * @property averageLatencyMs Average time taken per request in milliseconds.
 * @property lastRequestTimestamp Unix timestamp of the most recent request.
 */
data class ProviderMetrics(
    val requestCount: Long = 0,
    val errorCount: Long = 0,
    val averageLatencyMs: Double = 0.0,
    val lastRequestTimestamp: Long = 0
)
