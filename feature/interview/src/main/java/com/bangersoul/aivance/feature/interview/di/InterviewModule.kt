package com.bangersoul.aivance.feature.interview.di

import com.bangersoul.aivance.feature.interview.data.InterviewRepositoryImpl
import com.bangersoul.aivance.feature.interview.domain.InterviewRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class InterviewModule {

    @Binds
    @Singleton
    abstract fun bindInterviewRepository(
        interviewRepositoryImpl: InterviewRepositoryImpl
    ): InterviewRepository
}
