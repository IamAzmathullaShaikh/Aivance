package com.bangersoul.aivance.feature.ats.data

import com.bangersoul.aivance.core.database.dao.AtsDao
import com.bangersoul.aivance.core.database.model.AtsResultEntity
import com.bangersoul.aivance.feature.ats.domain.AtsRepository
import com.bangersoul.aivance.feature.ats.domain.AtsResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class AtsRepositoryImpl @Inject constructor(
    private val atsDao: AtsDao
) : AtsRepository {

    override fun getAtsResults(): Flow<List<AtsResult>> {
        return atsDao.getAtsResults().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAtsResultById(id: Long): AtsResult? {
        return atsDao.getAtsResultById(id)?.toDomain()
    }

    override suspend fun saveAtsResult(atsResult: AtsResult) {
        atsDao.insertAtsResult(atsResult.toEntity())
    }

    override suspend fun deleteAtsResult(atsResult: AtsResult) {
        atsDao.deleteAtsResult(atsResult.toEntity())
    }
}

private fun AtsResultEntity.toDomain() = AtsResult(
    id = id,
    score = score,
    date = Instant.ofEpochMilli(date),
    resumeName = resumeName,
    missingKeywords = if (missingKeywords.isEmpty()) emptyList() else missingKeywords.split(","),
    feedback = feedback
)

private fun AtsResult.toEntity() = AtsResultEntity(
    id = id,
    score = score,
    date = date.toEpochMilli(),
    resumeName = resumeName,
    missingKeywords = missingKeywords.joinToString(","),
    feedback = feedback
)
