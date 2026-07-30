package com.bangersoul.aivance.core.data.cache

import com.bangersoul.aivance.core.data.util.Clock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class MemoryCacheTest {

    private lateinit var clock: TestClock
    private lateinit var cache: MemoryCache<String, String>

    @Before
    fun setup() {
        clock = TestClock()
        cache = MemoryCache(clock)
    }

    @Test
    fun `put and get should return stored value`() {
        cache.put("key", "value")
        assertEquals("value", cache.get("key"))
    }

    @Test
    fun `get should return null if key does not exist`() {
        assertNull(cache.get("non-existent"))
    }

    @Test
    fun `evict should remove value from cache`() {
        cache.put("key", "value")
        cache.evict("key")
        assertNull(cache.get("key"))
    }

    @Test
    fun `clear should remove all values`() {
        cache.put("key1", "value1")
        cache.put("key2", "value2")
        cache.clear()
        assertNull(cache.get("key1"))
        assertNull(cache.get("key2"))
    }

    @Test
    fun `get should return null if entry is expired`() {
        cache.put("key", "value", ttlMillis = 1000)
        
        clock.advanceTime(500)
        assertEquals("value", cache.get("key"))
        
        clock.advanceTime(501)
        assertNull(cache.get("key"))
    }

    @Test
    fun `expired entry should be removed from underlying map on get`() {
        cache.put("key", "value", ttlMillis = 1000)
        clock.advanceTime(1001)
        
        assertNull(cache.get("key"))
    }

    private class TestClock(private var currentTime: Long = 1000) : Clock {
        override fun now(): Long = currentTime
        fun advanceTime(millis: Long) {
            currentTime += millis
        }
    }
}
