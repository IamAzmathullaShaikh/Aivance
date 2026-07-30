package com.bangersoul.aivance.core.data.di

import com.bangersoul.aivance.core.data.repository.AiRepositoryImpl
import com.bangersoul.aivance.core.data.repository.AnalyticsRepositoryImpl
import com.bangersoul.aivance.core.data.repository.CoverLetterRepositoryImpl
import com.bangersoul.aivance.core.data.repository.InterviewRepositoryImpl
import com.bangersoul.aivance.core.data.repository.JobRepositoryImpl
import com.bangersoul.aivance.core.data.repository.JobTrackerRepositoryImpl
import com.bangersoul.aivance.core.data.repository.ResumeRepositoryImpl
import com.bangersoul.aivance.core.data.repository.SearchRepositoryImpl
import com.bangersoul.aivance.core.data.repository.SettingsRepositoryImpl
import com.bangersoul.aivance.core.data.repository.UserRepositoryImpl
import com.bangersoul.aivance.core.domain.repository.AiRepository
import com.bangersoul.aivance.core.domain.repository.AnalyticsRepository
import com.bangersoul.aivance.core.domain.repository.CoverLetterRepository
import com.bangersoul.aivance.core.domain.repository.InterviewRepository
import com.bangersoul.aivance.core.domain.repository.JobRepository
import com.bangersoul.aivance.core.domain.repository.JobTrackerRepository
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.core.domain.repository.SearchRepository
import com.bangersoul.aivance.core.domain.repository.SettingsRepository
import com.bangersoul.aivance.core.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAiRepository(
        aiRepositoryImpl: AiRepositoryImpl
    ): AiRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(
        analyticsRepositoryImpl: AnalyticsRepositoryImpl
    ): AnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindCoverLetterRepository(
        coverLetterRepositoryImpl: CoverLetterRepositoryImpl
    ): CoverLetterRepository

    @Binds
    @Singleton
    abstract fun bindInterviewRepository(
        interviewRepositoryImpl: InterviewRepositoryImpl
    ): InterviewRepository

    @Binds
    @Singleton
    abstract fun bindJobRepository(
        jobRepositoryImpl: JobRepositoryImpl
    ): JobRepository

    @Binds
    @Singleton
    abstract fun bindJobTrackerRepository(
        jobTrackerRepositoryImpl: JobTrackerRepositoryImpl
    ): JobTrackerRepository

    @Binds
    @Singleton
    abstract fun bindResumeRepository(
        resumeRepositoryImpl: ResumeRepositoryImpl
    ): ResumeRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        searchRepositoryImpl: SearchRepositoryImpl
    ): SearchRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}
