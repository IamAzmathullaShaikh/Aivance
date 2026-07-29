package com.bangersoul.aivance.feature.ats.domain

import kotlinx.coroutines.flow.Flow

interface AtsRepository {
    fun getAtsResults(): Flow<List<AtsResult>>
    suspend fun getAtsResultById(id: Long): AtsResult?
    suspend fun saveAtsResult(atsResult: AtsResult)
    suspend fun deleteAtsResult(atsResult: AtsResult)
}
