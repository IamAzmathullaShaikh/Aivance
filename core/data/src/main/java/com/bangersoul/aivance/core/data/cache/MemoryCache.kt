package com.bangersoul.aivance.core.data.cache

import com.bangersoul.aivance.core.data.util.Clock
import java.util.concurrent.ConcurrentHashMap

/**
 * A thread-safe in-memory cache implementation using [ConcurrentHashMap].
 * Supports Time-To-Live (TTL) for each entry.
 */
class MemoryCache<K : Any, V : Any>(
    private val clock: Clock,
    private val defaultTtlMillis: Long = Long.MAX_VALUE
) : CacheManager<K, V> {

    private val cache = ConcurrentHashMap<K, CacheEntry<V>>()

    override fun get(key: K): V? {
        val entry = cache[key] ?: return null
        
        return if (isExpired(entry)) {
            cache.remove(key)
            null
        } else {
            entry.value
        }
    }

    override fun put(key: K, value: V, ttlMillis: Long?) {
        val ttl = ttlMillis ?: defaultTtlMillis
        val expiryTime = if (ttl == Long.MAX_VALUE) {
            Long.MAX_VALUE
        } else {
            val calculated = clock.now() + ttl
            if (calculated < 0) Long.MAX_VALUE else calculated
        }
        cache[key] = CacheEntry(value, expiryTime)
    }

    override fun evict(key: K) {
        cache.remove(key)
    }

    override fun clear() {
        cache.clear()
    }

    private fun isExpired(entry: CacheEntry<V>): Boolean {
        return clock.now() >= entry.expiryTime
    }

    private data class CacheEntry<V>(
        val value: V,
        val expiryTime: Long
    )
}
