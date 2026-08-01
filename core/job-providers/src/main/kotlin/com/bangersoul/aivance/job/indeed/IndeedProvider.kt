package com.bangersoul.aivance.job.indeed

import com.bangersoul.aivance.job.apify.ApifyJobProvider
import com.bangersoul.aivance.job.cache.JobCache
import com.bangersoul.aivance.sdk.core.ConfigField
import com.bangersoul.aivance.sdk.core.FieldType
import com.bangersoul.aivance.sdk.core.ProviderMetadata
import com.bangersoul.aivance.sdk.core.ProviderType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

class IndeedProvider(
    apiKey: String,
    jobCache: JobCache,
    okHttpClient: OkHttpClient,
    baseRetrofit: Retrofit
) : ApifyJobProvider(
    metadata = ProviderMetadata(
        id = "indeed",
        name = "Indeed",
        type = ProviderType.JOB,
        version = "1.0.0",
        description = "Job listings from Indeed via Apify.",
        icon = "https://www.indeed.com/favicon.ico",
        author = "BangerSoul",
        configFields = listOf(
            ConfigField(
                key = "apiKey",
                label = "Apify API Key",
                isSensitive = true,
                fieldType = FieldType.PASSWORD
            )
        )
    ),
    apiKey = apiKey,
    actorId = "misceres~indeed-scraper",
    jobCache = jobCache,
    okHttpClient = okHttpClient,
    baseRetrofit = baseRetrofit
)
