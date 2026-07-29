package com.bangersoul.aivance.feature.resume.data.model

import com.bangersoul.aivance.feature.resume.domain.model.KeywordInfo
import com.bangersoul.aivance.feature.resume.domain.model.OptimizationTip
import com.bangersoul.aivance.feature.resume.domain.model.ResumeAnalysis
import kotlinx.serialization.Serializable

@Serializable
data class ResumeAnalysisDto(
    val matchScore: Int,
    val keywords: List<KeywordDto>,
    val tips: List<TipDto>,
)

@Serializable
data class KeywordDto(
    val text: String,
    val isMatched: Boolean
)

@Serializable
data class TipDto(
    val category: String,
    val description: String
)

fun ResumeAnalysisDto.toDomain() = ResumeAnalysis(
    matchScore = matchScore,
    keywords = keywords.map { it.toDomain() },
    tips = tips.map { it.toDomain() }
)

fun KeywordDto.toDomain() = KeywordInfo(
    text = text,
    isMatched = isMatched
)

fun TipDto.toDomain() = OptimizationTip(
    category = category,
    description = description
)
