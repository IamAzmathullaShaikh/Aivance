package com.bangersoul.aivance.core.domain.performance

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.LinkedHashMap
import kotlin.math.pow

/**
 * Performance optimization utilities for lazy loading,
 * debouncing, throttling, and caching strategies.
 */
object PerformanceUtils {

    /**
     * Emit values at a steady interval by polling [generator].
     * Unlike throttling, this does NOT drop intermediate values —
     * it simply re-reads the source periodically.
     */
    fun <T> pollingFlow(
        intervalMs: Long = 300L,
        generator: suspend () -> T
    ): Flow<T> = flow {
        while (true) {
            emit(generator())
            delay(intervalMs)
        }
    }

    /**
     * True throttle: collects values from [producer], but emits at most
     * once per [intervalMs]. Intermediate values are dropped — only the
     * latest value within each window is emitted.
     */
    fun <T> throttle(
        intervalMs: Long = 300L,
        producer: suspend (suspend (T) -> Unit) -> Unit
    ): Flow<T> = callbackFlow {
        var lastEmission = 0L
        var pending: T? = null
        var job: kotlinx.coroutines.Job? = null

        val emitFn: suspend (T) -> Unit = { value ->
            val now = System.currentTimeMillis()
            if (now - lastEmission >= intervalMs) {
                trySend(value)
                lastEmission = now
                pending = null
            } else {
                pending = value
                if (job == null || job?.isCompleted == true) {
                    job = launch {
                        delay(intervalMs - (now - lastEmission))
                        pending?.let { trySend(it) }
                        lastEmission = System.currentTimeMillis()
                        pending = null
                    }
                }
            }
        }

        launch { producer(emitFn) }
        awaitClose { job?.cancel() }
    }

    /**
     * True debounce: emits a value only after [timeoutMs] of silence.
     * Each incoming value resets the timer.
     */
    fun <T> debounce(
        timeoutMs: Long = 400L,
        producer: suspend (suspend (T) -> Unit) -> Unit
    ): Flow<T> = callbackFlow {
        var job: kotlinx.coroutines.Job? = null

        val emitFn: suspend (T) -> Unit = { value ->
            job?.cancel()
            job = launch {
                delay(timeoutMs)
                trySend(value)
            }
        }

        launch { producer(emitFn) }
        awaitClose { job?.cancel() }
    }

    /**
     * Exponential backoff calculator for retry strategies.
     */
    object ExponentialBackoff {
        private const val DEFAULT_BASE_MS = 1000L
        private const val DEFAULT_MAX_MS = 60_000L
        private const val DEFAULT_MULTIPLIER = 2.0
        private const val DEFAULT_JITTER_FACTOR = 0.2

        fun delayMs(
            attempt: Int,
            baseMs: Long = DEFAULT_BASE_MS,
            maxMs: Long = DEFAULT_MAX_MS,
            multiplier: Double = DEFAULT_MULTIPLIER,
            jitterFactor: Double = DEFAULT_JITTER_FACTOR
        ): Long {
            val exp = baseMs * multiplier.pow(attempt.toDouble())
            val jitter = (exp * jitterFactor * Math.random()).toLong()
            return (exp.toLong() + jitter).coerceAtMost(maxMs)
        }
    }

    /**
     * Simple in-memory LRU cache with maximum size.
     */
    class LruCache<K, V>(private val maxSize: Int = 100) {
        private val cache = object : LinkedHashMap<K, V>(maxSize, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>): Boolean {
                return size > maxSize
            }
        }

        @Synchronized
        fun get(key: K): V? = cache[key]

        @Synchronized
        fun put(key: K, value: V): V? = cache.put(key, value)

        @Synchronized
        fun remove(key: K): V? = cache.remove(key)

        @Synchronized
        fun clear() = cache.clear()

        private val _sizeLock = Any()
        val size: Int get() = synchronized(_sizeLock) { cache.size }
    }

    /**
     * Batch processor that accumulates items and flushes them
     * when the batch size is reached or a timeout expires.
     */
    class BatchProcessor<T>(
        private val batchSize: Int = 50,
        private val flushTimeoutMs: Long = 5000L
    ) {
        private val buffer = mutableListOf<T>()
        private var lastFlushTime = System.currentTimeMillis()

        @Synchronized
        fun add(item: T, onFlush: (List<T>) -> Unit) {
            buffer.add(item)
            if (buffer.size >= batchSize || elapsed() >= flushTimeoutMs) {
                flush(onFlush)
            }
        }

        @Synchronized
        fun flush(onFlush: (List<T>) -> Unit) {
            if (buffer.isNotEmpty()) {
                onFlush(buffer.toList())
                buffer.clear()
                lastFlushTime = System.currentTimeMillis()
            }
        }

        private fun elapsed(): Long = System.currentTimeMillis() - lastFlushTime
    }
}

/**
 * Page size constants for optimal database and network pagination.
 */
object PageSizes {
    const val JOBS_PAGE_SIZE = 20
    const val RESUMES_PAGE_SIZE = 10
    const val CONVERSATIONS_PAGE_SIZE = 30
    const val MESSAGES_PAGE_SIZE = 50
    const val ANALYTICS_PAGE_SIZE = 100
    const val NOTIFICATIONS_PAGE_SIZE = 20
    const val TRACKER_PAGE_SIZE = 20
    const val INTERVIEWS_PAGE_SIZE = 10
    const val ANALYTICS_BATCH_SIZE = 50
    const val SYNC_BATCH_SIZE = 25
    const val CACHE_EVICTION_THRESHOLD = 100
}

/**
 * Timeout constants for network operations and background tasks.
 */
object TimeoutConstants {
    const val NETWORK_CONNECT_TIMEOUT_MS = 30_000L
    const val NETWORK_READ_TIMEOUT_MS = 30_000L
    const val NETWORK_WRITE_TIMEOUT_MS = 30_000L
    const val AI_STREAM_TIMEOUT_MS = 120_000L
    const val AI_GENERATION_TIMEOUT_MS = 60_000L
    const val WORKER_TIMEOUT_MS = 600_000L
    const val SYNC_TIMEOUT_MS = 120_000L
    const val DATABASE_TIMEOUT_MS = 10_000L
    const val CACHE_TTL_DEFAULT_MS = 300_000L
    const val CACHE_TTL_JOBS_MS = 600_000L
    const val CACHE_TTL_AI_RESPONSE_MS = 60_000L
}
