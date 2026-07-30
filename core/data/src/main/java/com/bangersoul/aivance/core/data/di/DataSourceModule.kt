package com.bangersoul.aivance.core.data.di

import com.bangersoul.aivance.core.data.source.AiLocalDataSource
import com.bangersoul.aivance.core.data.source.AiLocalDataSourceImpl
import com.bangersoul.aivance.core.data.source.CoverLetterLocalDataSource
import com.bangersoul.aivance.core.data.source.CoverLetterLocalDataSourceImpl
import com.bangersoul.aivance.core.data.source.InterviewLocalDataSource
import com.bangersoul.aivance.core.data.source.InterviewLocalDataSourceImpl
import com.bangersoul.aivance.core.data.source.JobLocalDataSource
import com.bangersoul.aivance.core.data.source.JobLocalDataSourceImpl
import com.bangersoul.aivance.core.data.source.ResumeLocalDataSource
import com.bangersoul.aivance.core.data.source.ResumeLocalDataSourceImpl
import com.bangersoul.aivance.core.data.source.SearchLocalDataSource
import com.bangersoul.aivance.core.data.source.SearchLocalDataSourceImpl
import com.bangersoul.aivance.core.data.source.SettingsLocalDataSource
import com.bangersoul.aivance.core.data.source.SettingsLocalDataSourceImpl
import com.bangersoul.aivance.core.data.source.UserLocalDataSource
import com.bangersoul.aivance.core.data.source.UserLocalDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindAiLocalDataSource(
        aiLocalDataSourceImpl: AiLocalDataSourceImpl
    ): AiLocalDataSource

    @Binds
    @Singleton
    abstract fun bindCoverLetterLocalDataSource(
        coverLetterLocalDataSourceImpl: CoverLetterLocalDataSourceImpl
    ): CoverLetterLocalDataSource

    @Binds
    @Singleton
    abstract fun bindInterviewLocalDataSource(
        interviewLocalDataSourceImpl: InterviewLocalDataSourceImpl
    ): InterviewLocalDataSource

    @Binds
    @Singleton
    abstract fun bindJobLocalDataSource(
        jobLocalDataSourceImpl: JobLocalDataSourceImpl
    ): JobLocalDataSource

    @Binds
    @Singleton
    abstract fun bindResumeLocalDataSource(
        resumeLocalDataSourceImpl: ResumeLocalDataSourceImpl
    ): ResumeLocalDataSource

    @Binds
    @Singleton
    abstract fun bindSearchLocalDataSource(
        searchLocalDataSourceImpl: SearchLocalDataSourceImpl
    ): SearchLocalDataSource

    @Binds
    @Singleton
    abstract fun bindSettingsLocalDataSource(
        settingsLocalDataSourceImpl: SettingsLocalDataSourceImpl
    ): SettingsLocalDataSource

    @Binds
    @Singleton
    abstract fun bindUserLocalDataSource(
        userLocalDataSourceImpl: UserLocalDataSourceImpl
    ): UserLocalDataSource
}
