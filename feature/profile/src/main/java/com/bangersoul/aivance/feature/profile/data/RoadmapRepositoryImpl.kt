package com.bangersoul.aivance.feature.profile.data

import com.bangersoul.aivance.core.database.dao.RoadmapDao
import com.bangersoul.aivance.core.database.model.RoadmapEntity
import com.bangersoul.aivance.core.database.model.RoadmapStepEntity
import com.bangersoul.aivance.core.database.model.RoadmapWithSteps
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.service.TextGenerationService
import com.bangersoul.aivance.feature.profile.domain.CareerRoadmap
import com.bangersoul.aivance.feature.profile.domain.RoadmapRepository
import com.bangersoul.aivance.feature.profile.domain.RoadmapStep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

class RoadmapRepositoryImpl @Inject constructor(
    private val roadmapDao: RoadmapDao,
    private val textGenerationService: TextGenerationService
) : RoadmapRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun generateRoadmap(targetRole: String, currentSkills: String): Flow<CareerRoadmap> = flow {
        val prompt = """
            Generate a detailed career roadmap to become a $targetRole starting with these current skills: $currentSkills.
            Provide a sequence of 5-8 actionable steps.
            Return the response ONLY as a JSON object with the following structure:
            {
              "steps": [
                {
                  "title": "Step Title",
                  "description": "Short description of what to do in this step"
                }
              ]
            }
        """.trimIndent()

        val result = textGenerationService.generateText(prompt)
        val text = when (result) {
            is Result.Success -> result.data
            is Result.Failure -> throw Exception(result.error.message)
        }
        val cleanResult = text.removeSurrounding("```json", "```").trim()
        val roadmapDto = json.decodeFromString<RoadmapDto>(cleanResult)

        val roadmapEntity = RoadmapEntity(
            targetRole = targetRole,
            currentLevel = "",
            description = "Career roadmap for $targetRole"
        )

        val stepEntities = roadmapDto.steps.mapIndexed { index, step ->
            RoadmapStepEntity(
                roadmapId = 0, // Set by DAO transaction
                title = step.title,
                description = step.description,
                stepOrder = index,
                isCompleted = false
            )
        }

        roadmapDao.insertRoadmapWithSteps(roadmapEntity, stepEntities)

        emitAll(getCurrentRoadmap().filterNotNull())
    }

    override fun getCurrentRoadmap(): Flow<CareerRoadmap?> {
        return roadmapDao.getCurrentRoadmapWithSteps().map { it?.asDomainModel() }
    }

    override fun toggleStep(roadmapId: Long, stepId: Long, isCompleted: Boolean): Flow<Unit> = flow {
        val currentRoadmap = roadmapDao.getCurrentRoadmapWithSteps().first()
        if (currentRoadmap != null && currentRoadmap.roadmap.id == roadmapId) {
            roadmapDao.updateStepCompletion(stepId, isCompleted)
        }
        emit(Unit)
    }

    private fun RoadmapWithSteps.asDomainModel(): CareerRoadmap {
        return CareerRoadmap(
            id = roadmap.id,
            targetRole = roadmap.targetRole,
            currentSkills = "",
            steps = steps.sortedBy { it.stepOrder }.map { it.asDomainModel() }
        )
    }

    private fun RoadmapStepEntity.asDomainModel(): RoadmapStep {
        return RoadmapStep(
            id = id,
            title = title,
            description = description,
            order = stepOrder,
            isCompleted = isCompleted
        )
    }

    @Serializable
    private data class RoadmapDto(
        val steps: List<StepDto>
    )

    @Serializable
    private data class StepDto(
        val title: String,
        val description: String
    )
}
