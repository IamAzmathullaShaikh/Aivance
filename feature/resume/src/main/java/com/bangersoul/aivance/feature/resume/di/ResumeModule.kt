package com.bangersoul.aivance.feature.resume.di

import com.bangersoul.aivance.feature.resume.data.repository.ResumeRepositoryImpl
import com.bangersoul.aivance.feature.resume.domain.repository.ResumeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ResumeModule {

    @Binds
    @Singleton
    abstract fun bindResumeRepository(
        resumeRepositoryImpl: ResumeRepositoryImpl
    ): ResumeRepository
}
