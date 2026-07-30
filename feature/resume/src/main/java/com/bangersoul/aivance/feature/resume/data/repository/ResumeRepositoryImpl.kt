package com.bangersoul.aivance.feature.resume.data.repository

import com.bangersoul.aivance.core.database.dao.AtsDao
import com.bangersoul.aivance.core.database.model.ResumeAnalysisEntity
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.service.TextGenerationService
import com.bangersoul.aivance.feature.resume.data.model.ResumeAnalysisDto
import com.bangersoul.aivance.feature.resume.data.model.toDomain
import com.bangersoul.aivance.feature.resume.domain.model.ResumeAnalysis
import com.bangersoul.aivance.feature.resume.domain.repository.ResumeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ResumeRepositoryImpl @Inject constructor(
    private val textGenerationService: TextGenerationService,
    private val atsDao: AtsDao
) : ResumeRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override fun analyzeResume(resumeText: String, jobDescription: String): Flow<ResumeAnalysis> = flow {
        val prompt = """
            Analyze the following resume against the job description.
            
            Resume:
            $resumeText
            
            Job Description:
            $jobDescription
            
            Provide the analysis in the following JSON format ONLY. Do not include any other text or markdown blocks.
            {
              "matchScore": (0-100 integer),
              "keywords": [
                {"text": "keyword", "isMatched": true/false}
              ],
              "tips": [
                {"category": "category name", "description": "detailed tip"}
              ]
            }
        """.trimIndent()

        val result = textGenerationService.generateText(prompt)
        val response = when (result) {
            is Result.Success -> result.data
            is Result.Failure -> throw Exception(result.error.message)
        }

        try {
            // Remove potential markdown formatting if the AI includes it
            val cleanJson = response.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            val dto = json.decodeFromString<ResumeAnalysisDto>(cleanJson)
            emit(dto.toDomain())
        } catch (e: Exception) {
            throw Exception("Failed to parse AI response: ${e.message}")
        }
    }

    override suspend fun saveAnalysis(analysis: ResumeAnalysis, resumeId: Long, jobDescription: String) {
        val entity = ResumeAnalysisEntity(
            resumeId = resumeId,
            jobDescription = jobDescription,
            score = analysis.matchScore,
            date = System.currentTimeMillis(),
            matchedKeywords = analysis.keywords.filter { it.isMatched }.joinToString(", ") { it.text },
            missingKeywords = analysis.keywords.filter { !it.isMatched }.joinToString(", ") { it.text },
            feedback = analysis.tips.joinToString("\n") { "${it.category}: ${it.description}" }
        )
        atsDao.insertAtsResult(entity)
    }
}
