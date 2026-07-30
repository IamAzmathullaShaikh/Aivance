package com.bangersoul.aivance.core.data.source

import com.bangersoul.aivance.core.common.model.CareerRoadmap
import com.bangersoul.aivance.core.common.model.UserProfile
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.database.dao.ProfileDao
import com.bangersoul.aivance.core.database.dao.RoadmapDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface UserLocalDataSource {
    fun getUserProfile(): Flow<UserProfile?>
    suspend fun saveUserProfile(profile: UserProfile)
    
    fun getRoadmaps(): Flow<List<CareerRoadmap>>
    fun getCurrentRoadmap(): Flow<CareerRoadmap?>
    suspend fun saveRoadmap(roadmap: CareerRoadmap)
    suspend fun updateRoadmapStep(stepId: Long, isCompleted: Boolean)
    suspend fun deleteRoadmap(roadmapId: Long)
}

class UserLocalDataSourceImpl @Inject constructor(
    private val profileDao: ProfileDao,
    private val roadmapDao: RoadmapDao
) : UserLocalDataSource {

    override fun getUserProfile(): Flow<UserProfile?> {
        return profileDao.getUserProfile().map { it?.toDomain() }
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        profileDao.insertProfile(profile.toEntity())
    }

    override fun getRoadmaps(): Flow<List<CareerRoadmap>> {
        return roadmapDao.getRoadmapsWithSteps().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCurrentRoadmap(): Flow<CareerRoadmap?> {
        return roadmapDao.getCurrentRoadmapWithSteps().map { it?.toDomain() }
    }

    override suspend fun saveRoadmap(roadmap: CareerRoadmap) {
        val entity = roadmap.toEntity()
        val steps = roadmap.steps.map { it.toEntity(roadmap.id) }
        roadmapDao.insertRoadmapWithSteps(entity, steps)
    }

    override suspend fun updateRoadmapStep(stepId: Long, isCompleted: Boolean) {
        roadmapDao.updateStepCompletion(stepId, isCompleted)
    }

    override suspend fun deleteRoadmap(roadmapId: Long) {
        roadmapDao.deleteRoadmapById(roadmapId)
    }
}
