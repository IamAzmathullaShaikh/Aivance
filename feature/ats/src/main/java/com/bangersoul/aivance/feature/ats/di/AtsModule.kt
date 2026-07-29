package com.bangersoul.aivance.feature.ats.di

import com.bangersoul.aivance.feature.ats.data.AtsRepositoryImpl
import com.bangersoul.aivance.feature.ats.domain.AtsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AtsModule {

    @Binds
    @Singleton
    abstract fun bindAtsRepository(
        impl: AtsRepositoryImpl
    ): AtsRepository
}
