package com.bangersoul.aivance.job.base

import com.bangersoul.aivance.core.common.enums.JobSortOrder
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.JobSearchFilter
import com.bangersoul.aivance.core.common.result.ProviderError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.job.cache.JobCache
import com.bangersoul.aivance.sdk.api.JobProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.core.ProviderMetadata
import com.bangersoul.aivance.sdk.core.ProviderStatus
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

/**
 * Base class for REST-based job providers.
 * Implements caching, retries via [RetryInterceptor], and basic circuit breaker logic.
 */
abstract class RestJobProvider(
    metadata: ProviderMetadata,
    capabilities: Set<ProviderCapability>,
    protected val jobCache: JobCache,
    protected val baseOkHttpClient: OkHttpClient,
    protected val baseRetrofit: Retrofit,
    private val errorThreshold: Int = 3
) : JobProvider(metadata, capabilities) {

    private val consecutiveErrors = AtomicInteger(0)

    /**
     * Base URL for the provider's API.
     */
    abstract val baseUrl: String

    /**
     * OkHttpClient configured with [RetryInterceptor] and other common configurations.
     */
    protected val okHttpClient: OkHttpClient by lazy {
        baseOkHttpClient.newBuilder()
            .addInterceptor(RetryInterceptor())
            .build()
    }

    /**
     * Retrofit instance configured with the provider's base URL and [okHttpClient].
     */
    protected val retrofit: Retrofit by lazy {
        baseRetrofit.newBuilder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .build()
    }

    override suspend fun searchJobs(
        filter: JobSearchFilter,
        sortOrder: JobSortOrder,
        page: Int
    ): Result<List<JobListing>> {
        // If the provider is in a hard Error state, don't even try
        if (status == ProviderStatus.Error) {
            return Result.Failure(ProviderError(metadata.id, message = "Provider is in Error state"))
        }

        return try {
            val jobs = executeSearch(filter, sortOrder, page)

            // Success: Reset error counter and restore status if needed
            consecutiveErrors.set(0)
            if (status == ProviderStatus.Degraded) {
                updateStatus(ProviderStatus.Active)
            }

            // Cache results for reliability
            jobCache.saveJobs(jobs)

            Result.Success(jobs)
        } catch (e: Exception) {
            handleFailure(e)

            // Reliability: Fallback to cache if network fails
            val cachedJobs = jobCache.getJobs()
            if (cachedJobs.isNotEmpty()) {
                Timber.d("Network failed for ${metadata.id}, returning ${cachedJobs.size} cached jobs")
                Result.Success(cachedJobs)
            } else {
                Result.Failure(ProviderError(metadata.id, message = e.message ?: "Unknown error", cause = e))
            }
        }
    }

    override suspend fun getJobDetails(jobId: String): Result<JobListing> {
        // 1. Try Cache
        val cachedJob = jobCache.getJobs().find { it.id == jobId }
        if (cachedJob != null) return Result.Success(cachedJob)

        // 2. Try Network if implemented by subclass
        return try {
            val job = executeGetDetails(jobId)
            if (job != null) {
                jobCache.saveJobs(listOf(job))
                Result.Success(job)
            } else {
                Result.Failure(ProviderError(metadata.id, message = "Job $jobId not found in network or cache"))
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to fetch job details from network for ${metadata.id}")
            Result.Failure(ProviderError(metadata.id, message = e.message ?: "Failed to fetch job details", cause = e))
        }
    }

    /**
     * Hook for subclasses to implement fetching a single job by ID from the network.
     */
    protected open suspend fun executeGetDetails(jobId: String): JobListing? = null

    /**
     * Implementation-specific search logic using [okHttpClient].
     */
    protected abstract suspend fun executeSearch(
        filter: JobSearchFilter,
        sortOrder: JobSortOrder,
        page: Int
    ): List<JobListing>

    /**
     * Handles request failures and implements circuit breaker logic.
     */
    private fun handleFailure(e: Exception) {
        val errors = consecutiveErrors.incrementAndGet()
        Timber.w(e, "Provider ${metadata.id} request failed ($errors/$errorThreshold)")

        if (errors >= errorThreshold) {
            Timber.e("Circuit breaker tripped for ${metadata.id}. Setting status to Degraded.")
            updateStatus(ProviderStatus.Degraded)
        }
    }

    override suspend fun checkHealth(): ProviderStatus {
        return try {
            performHealthCheck()

            // If health check succeeds, reset errors and mark as Active
            consecutiveErrors.set(0)
            if (status == ProviderStatus.Degraded || status == ProviderStatus.Error) {
                updateStatus(ProviderStatus.Active)
            }
            ProviderStatus.Active
        } catch (e: Exception) {
            handleFailure(e)
            // Honest failure: a throwing health check (bad key, unreachable host)
            // must surface as Degraded so validateProvider rejects bad credentials
            // (anything that isn't Ready/Active fails validation) instead of silently
            // passing via the previous status (typically Ready).
            //
            // We deliberately use Degraded, NOT Error: searchJobs hard-blocks on
            // Error with no automatic recovery path, so a single transient network
            // blip during validation would permanently disable the provider.
            // Degraded keeps the provider searchable and it self-recovers on the
            // next successful search (restored to Active).
            if (status != ProviderStatus.Error) {
                updateStatus(ProviderStatus.Degraded)
            }
            ProviderStatus.Degraded
        }
    }

    /**
     * Hook for subclasses to implement actual health check (e.g., pinging an endpoint).
     */
    protected open suspend fun performHealthCheck() {
        // Default: no-op, assumes healthy if no exceptions
    }

    override suspend fun onInitialize() {
        updateStatus(ProviderStatus.Initializing)
        // Subclasses can override to perform setup
        updateStatus(ProviderStatus.Ready)
    }

    override suspend fun onStart() {
        updateStatus(ProviderStatus.Active)
    }

    override suspend fun onStop() {
        updateStatus(ProviderStatus.Ready)
    }

    override suspend fun onDispose() {
        updateStatus(ProviderStatus.Disposed)
    }
}
