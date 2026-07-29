package com.bangersoul.aivance.feature.profile.domain

import kotlinx.coroutines.flow.Flow

interface RoadmapRepository {
    fun generateRoadmap(targetRole: String, currentSkills: String): Flow<CareerRoadmap>
    fun getCurrentRoadmap(): Flow<CareerRoadmap?>
    fun toggleStep(roadmapId: Long, stepId: Long, isCompleted: Boolean): Flow<Unit>
}
