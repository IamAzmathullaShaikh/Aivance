package com.bangersoul.aivance.core.data.util

/**
 * Interface to provide the current time, facilitating unit testing of time-dependent logic.
 */
interface Clock {
    fun now(): Long
}

/**
 * Default implementation of [Clock] using [System.currentTimeMillis].
 */
class DefaultClock : Clock {
    override fun now(): Long = System.currentTimeMillis()
}
