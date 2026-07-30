package com.bangersoul.aivance.job.linkedin

import com.bangersoul.aivance.job.apify.ApifyJobProvider
import com.bangersoul.aivance.job.cache.JobCache
import com.bangersoul.aivance.sdk.core.ProviderMetadata
import okhttp3.OkHttpClient
import retrofit2.Retrofit

class LinkedInProvider(
    apiKey: String,
    jobCache: JobCache,
    okHttpClient: OkHttpClient,
    retrofit: Retrofit
) : ApifyJobProvider(
    metadata = ProviderMetadata(
        id = "linkedin",
        name = "LinkedIn",
        version = "1.0.0",
        description = "Job listings from LinkedIn via Apify.",
        icon = "https://www.linkedin.com/favicon.ico",
        author = "BangerSoul"
    ),
    apiKey = apiKey,
    actorId = "linkedin-jobs-scraper",
    jobCache = jobCache,
    okHttpClient = okHttpClient,
    retrofit = retrofit
)
