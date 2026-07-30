package com.bangersoul.aivance.job.di

import com.bangersoul.aivance.job.cache.JobCache
import com.bangersoul.aivance.job.cache.RoomJobCache
import com.bangersoul.aivance.job.greenhouse.GreenhouseProvider
import com.bangersoul.aivance.job.indeed.IndeedProvider
import com.bangersoul.aivance.job.lever.LeverProvider
import com.bangersoul.aivance.job.linkedin.LinkedInProvider
import com.bangersoul.aivance.job.remoteok.RemoteOKProvider
import com.bangersoul.aivance.job.remotive.RemotiveProvider
import com.bangersoul.aivance.sdk.api.JobProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class JobProvidersModule {

    @Binds
    @Singleton
    abstract fun bindJobCache(impl: RoomJobCache): JobCache

    companion object {
        @Provides
        @Singleton
        @IntoSet
        fun provideRemoteOKProvider(
            jobCache: JobCache,
            okHttpClient: OkHttpClient,
            retrofit: Retrofit
        ): JobProvider {
            return RemoteOKProvider(jobCache, okHttpClient, retrofit)
        }

        @Provides
        @Singleton
        @IntoSet
        fun provideRemotiveProvider(
            jobCache: JobCache,
            okHttpClient: OkHttpClient,
            retrofit: Retrofit
        ): JobProvider {
            return RemotiveProvider(jobCache, okHttpClient, retrofit)
        }

        @Provides
        @Singleton
        @IntoSet
        fun provideLinkedInProvider(
            jobCache: JobCache,
            okHttpClient: OkHttpClient,
            retrofit: Retrofit
        ): JobProvider {
            return LinkedInProvider("LINKEDIN_API_KEY", jobCache, okHttpClient, retrofit)
        }

        @Provides
        @Singleton
        @IntoSet
        fun provideIndeedProvider(
            jobCache: JobCache,
            okHttpClient: OkHttpClient,
            retrofit: Retrofit
        ): JobProvider {
            return IndeedProvider("INDEED_API_KEY", jobCache, okHttpClient, retrofit)
        }

        @Provides
        @Singleton
        @IntoSet
        fun provideGreenhouseProvider(
            jobCache: JobCache,
            okHttpClient: OkHttpClient,
            retrofit: Retrofit
        ): JobProvider {
            return GreenhouseProvider("GREENHOUSE_TOKEN", jobCache, okHttpClient, retrofit)
        }

        @Provides
        @Singleton
        @IntoSet
        fun provideLeverProvider(
            jobCache: JobCache,
            okHttpClient: OkHttpClient,
            retrofit: Retrofit
        ): JobProvider {
            return LeverProvider("LEVER_COMPANY_ID", jobCache, okHttpClient, retrofit)
        }
    }
}
