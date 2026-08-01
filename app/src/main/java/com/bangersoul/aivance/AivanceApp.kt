package com.bangersoul.aivance

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.bangersoul.aivance.core.data.telemetry.StructuredTimberTree
import com.bangersoul.aivance.core.domain.repository.ProviderRepository
import com.bangersoul.aivance.core.domain.telemetry.TelemetryEngine
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import com.bangersoul.aivance.worker.AnalyticsUploadWorker
import com.bangersoul.aivance.worker.CacheCleanupWorker
import com.bangersoul.aivance.worker.DatabaseCleanupWorker
import com.bangersoul.aivance.worker.HealthCheckWorker
import com.bangersoul.aivance.worker.JobSyncWorker
import com.bangersoul.aivance.worker.ProviderRefreshWorker
import com.bangersoul.aivance.worker.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Application entry point for Aivance.
 *
 * Responsibilities:
 * - Configure Hilt dependency injection
 * - Register all periodic background workers via WorkManager
 * - Initialize Timber with debug + structured logging trees
 * - Initialize PerformanceCollector for runtime metrics
 */
@HiltAndroidApp
class AivanceApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var telemetryEngine: TelemetryEngine

    @Inject
    lateinit var providerManager: ProviderManager

    @Inject
    lateinit var providerRepository: ProviderRepository

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    override fun onCreate() {
        super.onCreate()
        initializeLogging()
        checkEnvironmentIntegrity()
        scheduleAllPeriodicWorkers()

        // Initialize AI/Job providers in background and re-apply any credentials
        // the user saved via onboarding/settings. DI constructs fresh provider
        // singletons with empty credentials on every launch, so hydration is what
        // makes a configured Groq/Indeed key survive app restarts.
        val scope = kotlinx.coroutines.MainScope()
        scope.launch(Dispatchers.IO + SupervisorJob()) {
            providerManager.initializeAll()
            hydrateSavedProviderConfigs()
        }
    }

    /**
     * Re-applies persisted provider configurations (including secrets) to the
     * live DI-singleton provider instances after startup initialization.
     */
    private suspend fun hydrateSavedProviderConfigs() {
        try {
            val savedConfigs = providerRepository.getProviderConfigs().firstOrNull() ?: emptyList()
            savedConfigs.forEach { saved ->
                // getProviderConfigs() intentionally omits secrets; fetch the full
                // config (which loads the API key from the encrypted DataStore).
                val fullConfig = providerRepository.getProviderConfig(saved.providerId) ?: return@forEach
                providerManager.reconfigure(saved.providerId, fullConfig)
            }
            if (savedConfigs.isNotEmpty()) {
                Timber.i("Hydrated ${savedConfigs.size} saved provider config(s)")
            }
        } catch (e: Exception) {
            Timber.w(e, "Provider config hydration failed")
        }
    }

    /**
     * Checks device environment integrity for security monitoring.
     * Logs warnings for rooted or emulated environments but does
     * not block app usage — maintains compatibility with development
     * and testing setups.
     */
    private fun checkEnvironmentIntegrity() {
        val isRooted = com.bangersoul.aivance.core.network.security.SecurityUtils.isDeviceRooted(this)
        val isEmulator = com.bangersoul.aivance.core.network.security.SecurityUtils.isDebuggableEnvironment(this)
        if (isRooted) {
            Timber.w("Running on rooted device — security features limited")
        }
        if (isEmulator) {
            Timber.i("Running in emulated environment")
        }
    }

    /**
     * Plants Timber trees for structured logging.
     *
     * - Debug builds: DebugTree (logcat) + StructuredTimberTree (telemetry pipeline)
     * - Release builds: Only StructuredTimberTree (production telemetry)
     */
    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.plant(StructuredTimberTree(telemetryEngine))
        Timber.i("Aivance logging initialized (debug=%s)", BuildConfig.DEBUG)
    }

    /**
     * Registers all periodic background workers.
     *
     * Uses [ExistingPeriodicWorkPolicy.KEEP] to avoid re-enqueuing
     * on every app restart — the system preserves existing schedules.
     */
    private fun scheduleAllPeriodicWorkers() {
        val workManager = WorkManager.getInstance(this)

        // 1. Sync Worker — every 15 minutes, needs connectivity
        workManager.enqueueUniquePeriodicWork(
            "periodic_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            java.util.concurrent.TimeUnit.MINUTES.toPeriodicWorkRequest<SyncWorker>(15) {
                setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                addTag("periodic_sync")
            }
        )

        // 2. Job Sync Worker — every 2 hours, Wi-Fi only, device idle
        workManager.enqueueUniquePeriodicWork(
            "periodic_job_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            java.util.concurrent.TimeUnit.HOURS.toPeriodicWorkRequest<JobSyncWorker>(2) {
                setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.UNMETERED)
                        .setRequiresBatteryNotLow(true)
                        .setRequiresDeviceIdle(true)
                        .build()
                )
                addTag("periodic_job_sync")
            }
        )

        // 3. Provider Refresh Worker — every 6 hours, needs connectivity
        workManager.enqueueUniquePeriodicWork(
            "periodic_provider_refresh",
            ExistingPeriodicWorkPolicy.KEEP,
            java.util.concurrent.TimeUnit.HOURS.toPeriodicWorkRequest<ProviderRefreshWorker>(6) {
                setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                addTag("periodic_provider_refresh")
            }
        )

        // 4. Analytics Upload Worker — every hour, Wi-Fi only
        workManager.enqueueUniquePeriodicWork(
            "periodic_analytics_upload",
            ExistingPeriodicWorkPolicy.KEEP,
            java.util.concurrent.TimeUnit.HOURS.toPeriodicWorkRequest<AnalyticsUploadWorker>(1) {
                setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.UNMETERED)
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                addTag("periodic_analytics_upload")
            }
        )

        // 5. Cache Cleanup Worker — daily, charging, idle
        workManager.enqueueUniquePeriodicWork(
            "periodic_cache_cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            java.util.concurrent.TimeUnit.DAYS.toPeriodicWorkRequest<CacheCleanupWorker>(1) {
                setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiresDeviceIdle(true)
                        .setRequiresCharging(true)
                        .build()
                )
                addTag("periodic_cache_cleanup")
            }
        )

        // 6. Database Cleanup Worker — weekly, charging, idle
        workManager.enqueueUniquePeriodicWork(
            "periodic_db_cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            java.util.concurrent.TimeUnit.DAYS.toPeriodicWorkRequest<DatabaseCleanupWorker>(7) {
                setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiresDeviceIdle(true)
                        .setRequiresCharging(true)
                        .build()
                )
                addTag("periodic_db_cleanup")
            }
        )

        // 7. Health Check Worker — every 12 hours, charging, idle
        workManager.enqueueUniquePeriodicWork(
            "periodic_health_check",
            ExistingPeriodicWorkPolicy.KEEP,
            java.util.concurrent.TimeUnit.HOURS.toPeriodicWorkRequest<HealthCheckWorker>(12) {
                setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiresDeviceIdle(true)
                        .setRequiresCharging(true)
                        .build()
                )
                addTag("periodic_health_check")
            }
        )

        Timber.i("All 7 periodic workers scheduled")
    }
}

// ── Extension: TimeUnit → PeriodicWorkRequest ───────

private inline fun <reified T : androidx.work.CoroutineWorker> java.util.concurrent.TimeUnit.toPeriodicWorkRequest(
    duration: Long,
    builder: androidx.work.PeriodicWorkRequest.Builder.() -> Unit
): androidx.work.PeriodicWorkRequest {
    val workBuilder = androidx.work.PeriodicWorkRequestBuilder<T>(duration, this)
    workBuilder.builder()
    return workBuilder.build()
}
