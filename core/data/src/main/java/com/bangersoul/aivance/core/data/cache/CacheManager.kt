package com.bangersoul.aivance.core.data.cache

/**
 * Generic cache interface for storing and retrieving data.
 */
interface CacheManager<K, V> {
    /**
     * Retrieves the value associated with the given [key].
     * Returns null if the key is not found or has expired.
     */
    fun get(key: K): V?

    /**
     * Associates the specified [value] with the specified [key] in the cache.
     * @param key Key with which the value is associated.
     * @param value Value to be stored.
     * @param ttlMillis Optional time-to-live in milliseconds. If null, the default TTL is used or it never expires.
     */
    fun put(key: K, value: V, ttlMillis: Long? = null)

    /**
     * Removes the entry for the specified [key] from the cache.
     */
    fun evict(key: K)

    /**
     * Clears all entries from the cache.
     */
    fun clear()
}
