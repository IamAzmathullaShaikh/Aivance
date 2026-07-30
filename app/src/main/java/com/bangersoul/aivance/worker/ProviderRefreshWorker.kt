package com.bangersoul.aivance.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.usecase.provider.GetAvailableModelsUseCase
import com.bangersoul.aivance.core.domain.usecase.provider.GetProviderHealthUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Periodic worker that checks the health of configured AI providers
 * and refreshes their available model lists.
 *
 * Scheduled every 6 hours. Also emitted as one-time work after a
 * provider configuration change.
 */
@HiltWorker
class ProviderRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getProviderHealthUseCase: GetProviderHealthUseCase,
    private val getAvailableModelsUseCase: GetAvailableModelsUseCase,
    private val connectivityMonitor: ConnectivityMonitor
) : CoroutineWorker(context, params) {

    companion object {
        val knownProviders = listOf("gemini", "openai", "groq", "openrouter", "ollama")
    }

    override suspend fun doWork(): Result {
        Timber.d("ProviderRefreshWorker started")

        if (!connectivityMonitor.isOnline) {
            Timber.d("ProviderRefreshWorker — offline, deferring")
            return Result.retry()
        }

        var healthyCount = 0
        var modelCount = 0

        try {
            for (providerId in knownProviders) {
                // Check health
                val healthResult = getProviderHealthUseCase(providerId)
                when (healthResult) {
                    is Result.Success -> {
                        if (healthResult.data) {
                            healthyCount++
                        }
                    }
                    is Result.Failure -> {
                        Timber.w("ProviderRefreshWorker — %s health check failed: %s",
                            providerId, healthResult.error.message)
                    }
                }

                // Refresh models
                val modelsResult = getAvailableModelsUseCase(providerId)
                when (modelsResult) {
                    is Result.Success -> {
                        modelCount += modelsResult.data.size
                    }
                    is Result.Failure -> {
                        Timber.w("ProviderRefreshWorker — %s model refresh failed: %s",
                            providerId, modelsResult.error.message)
                    }
                }
            }

            Timber.d("ProviderRefreshWorker completed — %d healthy providers, %d models",
                healthyCount, modelCount)
            Result.success()

        } catch (e: Exception) {
            Timber.e(e, "ProviderRefreshWorker failed")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
