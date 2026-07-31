package com.bangersoul.aivance.core.enrichment.di

import com.bangersoul.aivance.core.enrichment.hunter.HunterEnrichmentProvider
import com.bangersoul.aivance.sdk.api.EnrichmentProvider
import com.bangersoul.aivance.sdk.config.ProviderConfiguration
import com.bangersoul.aivance.sdk.infrastructure.ProviderFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import dagger.multibindings.StringKey
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EnrichmentProvidersModule {

    @Provides
    @Singleton
    @IntoSet
    fun provideHunterProvider(
        okHttpClient: OkHttpClient,
        retrofit: Retrofit
    ): EnrichmentProvider {
        return HunterEnrichmentProvider(ProviderConfiguration("hunter"), okHttpClient, retrofit)
    }

    @Provides
    @IntoMap
    @StringKey("hunter")
    fun provideHunterFactory(
        okHttpClient: OkHttpClient,
        retrofit: Retrofit
    ): ProviderFactory.Factory {
        return ProviderFactory.Factory { config ->
            HunterEnrichmentProvider(config.toProviderConfig("hunter"), okHttpClient, retrofit)
        }
    }

    private fun Map<String, Any>?.toProviderConfig(id: String): ProviderConfiguration {
        @Suppress("UNCHECKED_CAST")
        val settings = (this?.get("settings") as? Map<String, String>) ?: emptyMap()
        @Suppress("UNCHECKED_CAST")
        val secrets = (this?.get("secrets") as? Map<String, String>) ?: emptyMap()
        return ProviderConfiguration(id, settings, secrets)
    }
}
