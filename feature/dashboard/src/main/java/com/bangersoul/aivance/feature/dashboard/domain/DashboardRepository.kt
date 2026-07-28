package com.bangersoul.aivance.feature.dashboard.domain

import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getDashboardData(): Flow<DashboardData>
}
