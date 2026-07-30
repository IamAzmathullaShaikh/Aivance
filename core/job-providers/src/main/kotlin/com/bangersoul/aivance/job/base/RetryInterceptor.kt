package com.bangersoul.aivance.job.base

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import kotlin.math.pow

/**
 * Interceptor that retries failed requests with exponential backoff.
 */
class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val initialDelay: Long = 1000L,
    private val backoffMultiplier: Double = 2.0
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var lastException: IOException? = null
        
        for (attempt in 0..maxRetries) {
            if (attempt > 0) {
                val delay = (initialDelay * backoffMultiplier.pow((attempt - 1).toDouble())).toLong()
                Timber.d("Retrying request ${request.url} (attempt $attempt/$maxRetries) after ${delay}ms")
                try {
                    Thread.sleep(delay)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw IOException("Retry interrupted", e)
                }
            }

            try {
                response?.close()
                response = chain.proceed(request)
                if (response.isSuccessful) return response
                
                // Don't retry client errors (4xx) except maybe 408 or 429
                if (response.code in 400..499 && response.code != 408 && response.code != 429) {
                    return response
                }
            } catch (e: IOException) {
                lastException = e
                Timber.w(e, "Request failed: ${request.url} (attempt $attempt/$maxRetries)")
            }
        }

        return response ?: throw lastException ?: IOException("Request failed after $maxRetries retries")
    }
}
