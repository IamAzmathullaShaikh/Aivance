package com.bangersoul.aivance.core.network.di

import com.bangersoul.aivance.core.common.security.CertificatePins
import com.bangersoul.aivance.core.network.BuildConfig
import com.bangersoul.aivance.core.network.security.CertificatePinningInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.ConnectionSpec
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Network module providing OkHttp, Retrofit, and serialization infrastructure.
 *
 * The OkHttpClient is configured with:
 * - TLS 1.2+ only for secure connections
 * - Certificate pinning for defense-in-depth
 * - Logging interceptor (debug only)
 * - 30-second timeouts
 * - Connection retry on failure
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    fun provideCertificatePinningInterceptor(): CertificatePinningInterceptor {
        return CertificatePinningInterceptor()
    }

    @Provides
    @Singleton
    fun provideCertificatePinner(): okhttp3.CertificatePinner {
        val builder = okhttp3.CertificatePinner.Builder()
        CertificatePins.OKHTTP_PINS.forEach { (host, pins) ->
            builder.add(host, *pins.toTypedArray())
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        pinningInterceptor: CertificatePinningInterceptor,
        certificatePinner: okhttp3.CertificatePinner
    ): OkHttpClient {
        val connectionSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
            .allEnabledCipherSuites()
            .build()

        return OkHttpClient.Builder()
            .connectionSpecs(listOf(connectionSpec))
            // Native CertificatePinner runs during the TLS handshake — this is
            // the authoritative pinning enforcement (application interceptors
            // see a null connection on first request and cannot enforce).
            .certificatePinner(certificatePinner)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(pinningInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Shared Retrofit template (converter factory + client).
     *
     * The base URL here is a dummy and is deliberately NEVER used for real
     * requests: every provider rebuilds its own Retrofit from this template
     * via `baseRetrofit.newBuilder().baseUrl(<provider baseUrl>)`. Do not
     * "fix" this placeholder — it only needs to be a valid URL for Retrofit
     * construction.
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        json: Json,
        okHttpClient: OkHttpClient
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/") // Template only — never used for requests
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}
