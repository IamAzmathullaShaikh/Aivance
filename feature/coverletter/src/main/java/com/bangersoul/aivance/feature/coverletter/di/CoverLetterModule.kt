package com.bangersoul.aivance.feature.coverletter.di

import com.bangersoul.aivance.feature.coverletter.data.repository.CoverLetterRepositoryImpl
import com.bangersoul.aivance.feature.coverletter.domain.repository.CoverLetterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoverLetterModule {

    @Binds
    @Singleton
    abstract fun bindCoverLetterRepository(
        coverLetterRepositoryImpl: CoverLetterRepositoryImpl
    ): CoverLetterRepository
}
