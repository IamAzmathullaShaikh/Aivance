package com.bangersoul.aivance.feature.dashboard.di

import com.bangersoul.aivance.feature.dashboard.data.FakeDashboardRepository
import com.bangersoul.aivance.feature.dashboard.domain.DashboardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardModule {

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        fakeDashboardRepository: FakeDashboardRepository
    ): DashboardRepository
}
