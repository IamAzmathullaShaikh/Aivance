package com.bangersoul.aivance.job.indeed

import com.bangersoul.aivance.job.apify.ApifyJobProvider
import com.bangersoul.aivance.job.cache.JobCache
import com.bangersoul.aivance.sdk.core.ProviderMetadata
import okhttp3.OkHttpClient
import retrofit2.Retrofit

class IndeedProvider(
    apiKey: String,
    jobCache: JobCache,
    okHttpClient: OkHttpClient,
    retrofit: Retrofit
) : ApifyJobProvider(
    metadata = ProviderMetadata(
        id = "indeed",
        name = "Indeed",
        version = "1.0.0",
        description = "Job listings from Indeed via Apify.",
        icon = "https://www.indeed.com/favicon.ico",
        author = "BangerSoul"
    ),
    apiKey = apiKey,
    actorId = "indeed-scraper",
    jobCache = jobCache,
    okHttpClient = okHttpClient,
    retrofit = retrofit
)
