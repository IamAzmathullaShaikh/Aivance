package com.bangersoul.aivance.job.apify

import com.bangersoul.aivance.core.common.enums.JobSortOrder
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.JobSearchFilter
import com.bangersoul.aivance.core.common.result.ProviderError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.job.base.RestJobProvider
import com.bangersoul.aivance.job.cache.JobCache
import com.bangersoul.aivance.job.mapper.JobMapper
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.core.ProviderMetadata
import com.bangersoul.aivance.sdk.core.ProviderStatus
import com.bangersoul.aivance.sdk.core.ProviderType
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import timber.log.Timber

/**
 * Job provider implementation using Apify Actors for scraping job listings.
 *
 * Real flow: start an actor run -> poll until SUCCEEDED -> fetch the dataset items.
 */
open class ApifyJobProvider(
    metadata: ProviderMetadata,
    protected var apiKey: String,
    protected val actorId: String,
    jobCache: JobCache,
    okHttpClient: OkHttpClient,
    baseRetrofit: Retrofit,
    override val baseUrl: String = "https://api.apify.com/v2/"
) : RestJobProvider(
    metadata = metadata,
    capabilities = setOf(ProviderCapability.JobSearch),
    jobCache = jobCache,
    baseOkHttpClient = okHttpClient,
    baseRetrofit = baseRetrofit
) {
    private val api: ApifyApi by lazy { retrofit.create(ApifyApi::class.java) }

    override val isConfigured: Boolean
        get() = apiKey.isNotBlank()

    override val hasCredentials: Boolean
        get() = isConfigured

    override suspend fun applyConfiguration(config: com.bangersoul.aivance.sdk.config.ProviderConfiguration) {
        apiKey = config.secrets["apiKey"] ?: apiKey
    }

    private val maxPollAttempts = 30
    private val pollIntervalMs = 2_000L

    override suspend fun executeSearch(
        filter: JobSearchFilter,
        sortOrder: JobSortOrder,
        page: Int
    ): List<JobListing> {
        if (apiKey.isBlank() || actorId.isBlank()) {
            throw Exception("Apify API Key and Actor ID not configured")
        }

        // 1. Start the actor run with the search query as input.
        val input = buildJsonObject {
            if (filter.query.isNotBlank()) {
                put("search", JsonPrimitive(filter.query))
            }
            if (filter.location.isNotBlank()) {
                put("location", JsonPrimitive(filter.location))
            }
        }
        val runResponse = api.runActor(actorId, apiKey, input)
        if (!runResponse.isSuccessful) {
            throw Exception("Apify run failed to start: ${runResponse.code()}")
        }
        val runData = runResponse.body()?.data ?: throw Exception("Apify returned empty run data")
        Timber.d("Apify run started: ${runData.id} (${runData.status})")

        // 2. Poll until the run finishes (check immediately, then wait between polls).
        var runId = runData.id
        var datasetId = runData.defaultDatasetId
        var attempts = 0
        while (attempts < maxPollAttempts) {
            attempts++
            val statusResponse = api.getActorRun(runId, apiKey)
            if (!statusResponse.isSuccessful) {
                throw Exception("Apify run status failed: ${statusResponse.code()}")
            }
            val data = statusResponse.body()?.data ?: runData
            runId = data.id
            datasetId = data.defaultDatasetId ?: datasetId
            when (data.status) {
                "SUCCEEDED" -> break
                "FAILED", "ABORTED", "TIMED-OUT" ->
                    throw Exception("Apify run ${data.status} for $actorId")
            }
            if (attempts < maxPollAttempts) delay(pollIntervalMs)
        }
        if (attempts >= maxPollAttempts) {
            throw Exception("Apify run timed out after $maxPollAttempts polls")
        }

        // 3. Fetch the produced dataset.
        val datasetIdFinal = datasetId ?: throw Exception("Apify run produced no dataset")
        val itemsResponse = api.getDatasetItems(datasetIdFinal, apiKey, limit = 100, offset = (page - 1) * 100)
        if (!itemsResponse.isSuccessful) {
            throw Exception("Apify dataset fetch failed: ${itemsResponse.code()}")
        }
        return itemsResponse.body()?.map { JobMapper.mapToJobListing(it, metadata.id) } ?: emptyList()
    }



    override suspend fun onInitialize() {
        super.onInitialize()
        // Stay out of Active/Ready until the user provides real Apify credentials,
        // so search aggregation filters this provider (and its Indeed/LinkedIn
        // subclasses) out until configured.
        if (apiKey.isBlank() || actorId.isBlank()) {
            updateStatus(ProviderStatus.InvalidConfiguration)
        }
    }

    override suspend fun performHealthCheck() {
        if (apiKey.isBlank() || actorId.isBlank()) {
            throw Exception("Apify API Key and Actor ID not configured")
        }
        val request = okhttp3.Request.Builder()
            .url("${baseUrl}acts?token=$apiKey")
            .get()
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Apify service unreachable: HTTP ${response.code}")
            }
        }
    }
}
