package com.bangersoul.aivance.feature.tracker.di

import com.bangersoul.aivance.feature.tracker.data.JobTrackerRepositoryImpl
import com.bangersoul.aivance.feature.tracker.domain.JobTrackerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TrackerModule {

    @Binds
    @Singleton
    abstract fun bindJobTrackerRepository(
        impl: JobTrackerRepositoryImpl
    ): JobTrackerRepository
}
