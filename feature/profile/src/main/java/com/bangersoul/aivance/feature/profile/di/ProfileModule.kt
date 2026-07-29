package com.bangersoul.aivance.feature.profile.di

import com.bangersoul.aivance.feature.profile.data.RoadmapRepositoryImpl
import com.bangersoul.aivance.feature.profile.domain.RoadmapRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    @Singleton
    abstract fun bindRoadmapRepository(
        roadmapRepositoryImpl: RoadmapRepositoryImpl
    ): RoadmapRepository
}
