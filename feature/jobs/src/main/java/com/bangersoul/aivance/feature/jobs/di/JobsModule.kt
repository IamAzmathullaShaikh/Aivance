package com.bangersoul.aivance.feature.jobs.di

import com.bangersoul.aivance.feature.jobs.data.JobSearchRepositoryImpl
import com.bangersoul.aivance.feature.jobs.domain.JobSearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class JobsModule {

    @Binds
    @Singleton
    abstract fun bindJobSearchRepository(
        jobSearchRepositoryImpl: JobSearchRepositoryImpl
    ): JobSearchRepository
}
