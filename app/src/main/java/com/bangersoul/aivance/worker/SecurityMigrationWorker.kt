package com.bangersoul.aivance.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bangersoul.aivance.core.database.dao.AiAnalyticsDao
import com.bangersoul.aivance.core.database.model.ProviderConfigurationEntity
import com.bangersoul.aivance.core.datastore.SecretsManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * H-01 fix: Scans every `provider_configurations` row for plaintext API key values
 * that may have been written before MIGRATION_19_20 removed the dedicated `apiKey`
 * column. Any secrets found in the `settings` map (identified by well-known key names)
 * are silently migrated to [SecretsManager] encrypted storage and stripped from the
 * DB row, preventing PII leakage via database backups or adb pull.
 *
 * The worker is **idempotent** — once it finishes without error it returns
 * [Result.success] and WorkManager will not re-enqueue it (enqueued with
 * [ExistingWorkPolicy.KEEP]).
 */
@HiltWorker
class SecurityMigrationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val analyticsDao: AiAnalyticsDao,
    private val secretsManager: SecretsManager
) : CoroutineWorker(context, params) {

    /**
     * Settings keys that should never be stored in plaintext inside the DB.
     * If any of these appear in a provider's settings map the value is
     * migrated to SecretsManager and removed from the map.
     */
    private val sensitiveSettingsKeys = setOf(
        "apiKey", "api_key", "apikey",
        "token", "accessToken", "access_token",
        "secret", "clientSecret", "client_secret",
        "password", "authToken", "auth_token"
    )

    override suspend fun doWork(): Result {
        Timber.i("SecurityMigrationWorker: starting plaintext PII scan")
        return try {
            var migratedCount = 0
            val configs: List<ProviderConfigurationEntity> =
                analyticsDao.getAllProviderConfigs().first()

            for (config in configs) {
                val suspiciousKeys = config.settings.keys
                    .filter { it in sensitiveSettingsKeys }

                if (suspiciousKeys.isEmpty()) continue

                Timber.w(
                    "SecurityMigrationWorker: provider '%s' has %d plaintext key(s) in settings — migrating",
                    config.provider,
                    suspiciousKeys.size
                )

                // Migrate each plaintext secret to encrypted storage
                for (key in suspiciousKeys) {
                    val plainValue = config.settings[key] ?: continue
                    if (plainValue.isBlank()) continue

                    val secretKey = "provider.${config.provider}.$key"
                    secretsManager.saveSecret(secretKey, plainValue)
                    Timber.d(
                        "SecurityMigrationWorker: migrated '%s' for provider '%s' to SecretsManager",
                        key,
                        config.provider
                    )
                    migratedCount++
                }

                // Write back the row with sensitive keys stripped from the settings map
                val sanitizedSettings = config.settings
                    .filterKeys { it !in sensitiveSettingsKeys }
                val sanitizedConfig = config.copy(settings = sanitizedSettings)
                analyticsDao.insertProviderConfig(sanitizedConfig)
            }

            Timber.i(
                "SecurityMigrationWorker: scan complete — migrated %d secret(s) from %d provider config(s)",
                migratedCount,
                configs.size
            )
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "SecurityMigrationWorker: failed — will retry")
            Result.retry()
        }
    }
}
