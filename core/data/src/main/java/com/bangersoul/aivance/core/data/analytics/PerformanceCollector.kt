package com.bangersoul.aivance.core.data.analytics

import android.content.Context
import android.os.Debug
import android.os.Process
import com.bangersoul.aivance.core.common.dto.PerformanceMetric
import com.bangersoul.aivance.core.domain.telemetry.TelemetryEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedReader
import java.io.FileReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Collects runtime performance metrics on a periodic interval.
 *
 * Metrics collected:
 * - Memory usage (heap: used/free/total/native)
 * - CPU usage (process CPU via /proc/self/stat, thread count via /proc/self/status)
 * - Startup duration
 * - Database query latency
 * - Network request timing
 * - Compose recomposition counts
 * - Frame render time
 */
@Singleton
class PerformanceCollector @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val telemetryEngine: TelemetryEngine
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val startupTimestamp = System.currentTimeMillis()

    private val _appStartupTime = MutableStateFlow(0L)
    val appStartupTime: StateFlow<Long> = _appStartupTime.asStateFlow()

    private val _currentMemoryMb = MutableStateFlow(0.0)
    val currentMemoryMb: StateFlow<Double> = _currentMemoryMb.asStateFlow()

    init {
        _appStartupTime.value = startupTimestamp
        startPeriodicCollection()
    }

    fun recordStartupComplete() {
        val startupDuration = System.currentTimeMillis() - startupTimestamp
        telemetryEngine.recordMetric(
            PerformanceMetric("app.startup.duration", startupDuration.toDouble(), "ms",
                tags = mapOf("cold_start" to "true"))
        )
        Timber.d("App startup: %dms", startupDuration)
    }

    fun recordDatabaseQuery(queryName: String, durationMs: Long) {
        telemetryEngine.recordMetric(
            PerformanceMetric("database.query.$queryName.duration", durationMs.toDouble(), "ms"))
    }

    fun recordRecomposition(composableName: String, count: Int) {
        telemetryEngine.recordMetric(
            PerformanceMetric("compose.recomposition.$composableName", count.toDouble(), "count"))
    }

    fun recordNetworkRequest(endpoint: String, durationMs: Long, success: Boolean) {
        telemetryEngine.recordMetric(
            PerformanceMetric("network.request.$endpoint.duration", durationMs.toDouble(), "ms",
                tags = mapOf("success" to success.toString())))
    }

    fun recordFrameTime(frameTimeMs: Double) {
        telemetryEngine.recordMetric(PerformanceMetric("ui.frame_time", frameTimeMs, "ms"))
    }

    private fun startPeriodicCollection() {
        scope.launch {
            while (isActive) {
                collectMemoryMetrics()
                collectCpuMetrics()
                delay(30_000)
            }
        }
    }

    private fun collectMemoryMetrics() {
        try {
            val runtime = Runtime.getRuntime()
            val usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0)
            val freeMb = runtime.freeMemory() / (1024.0 * 1024.0)
            val maxMb = runtime.maxMemory() / (1024.0 * 1024.0)

            _currentMemoryMb.value = usedMb
            telemetryEngine.recordMetric(PerformanceMetric("memory.heap.used", usedMb, "MB"))
            telemetryEngine.recordMetric(PerformanceMetric("memory.heap.free", freeMb, "MB"))
            telemetryEngine.recordMetric(PerformanceMetric("memory.heap.max", maxMb, "MB"))

            val nativeUsed = Debug.getNativeHeapAllocatedSize() / (1024.0 * 1024.0)
            telemetryEngine.recordMetric(PerformanceMetric("memory.native.used", nativeUsed, "MB"))
        } catch (e: Exception) {
            Timber.w(e, "Failed to collect memory metrics")
        }
    }

    private fun collectCpuMetrics() {
        try {
            val pid = Process.myPid()
            val statReader = BufferedReader(FileReader("/proc/$pid/stat"))
            val stat = statReader.readLine()
            statReader.close()
            val fields = stat.split(" ")
            if (fields.size > 21) {
                val utime = fields[13].toLongOrNull() ?: 0L
                val stime = fields[14].toLongOrNull() ?: 0L
                telemetryEngine.recordMetric(PerformanceMetric("cpu.process.jiffies", (utime + stime).toDouble(), "jiffies"))
            }

            val statusReader = BufferedReader(FileReader("/proc/self/status"))
            val status = statusReader.readText()
            statusReader.close()
            val threadsLine = status.lines().find { it.startsWith("Threads:") }
            val threadCount = threadsLine?.split("\t")?.getOrNull(1)?.trim()?.toDoubleOrNull() ?: 0.0
            telemetryEngine.recordMetric(PerformanceMetric("cpu.threads", threadCount, "count"))

        } catch (e: Exception) {
            Timber.w(e, "Failed to collect CPU metrics")
        }
    }
}
