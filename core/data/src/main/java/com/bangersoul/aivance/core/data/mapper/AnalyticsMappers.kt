package com.bangersoul.aivance.core.data.mapper

import com.bangersoul.aivance.core.common.model.*
import com.bangersoul.aivance.core.database.model.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

private val jsonMapper = Json { ignoreUnknownKeys = true }

fun AnalyticsSnapshotEntity.toDomain(): AnalyticsSnapshot {
    return AnalyticsSnapshot(
        id = id,
        timestamp = timestamp,
        kpis = try { jsonMapper.decodeFromString(kpiJson) } catch (e: Exception) { emptyMap() },
        careerScore = careerScore,
        dimensionScores = try { jsonMapper.decodeFromString(dimensionScoresJson) } catch (e: Exception) { emptyMap() }
    )
}

fun AnalyticsSnapshot.toEntity(): AnalyticsSnapshotEntity {
    return AnalyticsSnapshotEntity(
        id = id,
        timestamp = timestamp,
        kpiJson = jsonMapper.encodeToString(kpis),
        careerScore = careerScore,
        dimensionScoresJson = jsonMapper.encodeToString(dimensionScores)
    )
}

fun RecommendationEntity.toDomain(): CareerRecommendation {
    return CareerRecommendation(
        id = id,
        title = title,
        description = description,
        priority = priority,
        category = category,
        actionDeepLink = actionDeepLink,
        evidence = try { jsonMapper.decodeFromString(evidenceJson ?: "{}") } catch (e: Exception) { emptyMap() },
        timestamp = timestamp
    )
}

fun CareerRecommendation.toEntity(): RecommendationEntity {
    return RecommendationEntity(
        id = id,
        title = title,
        description = description,
        priority = priority,
        category = category,
        actionDeepLink = actionDeepLink,
        evidenceJson = jsonMapper.encodeToString(evidence),
        isDismissed = false,
        timestamp = timestamp
    )
}

fun GoalEntity.toDomain(): CareerGoal {
    return CareerGoal(
        id = id,
        title = title,
        description = description,
        targetValue = targetValue,
        currentValue = currentValue,
        unit = unit,
        deadline = deadline,
        isCompleted = isCompleted,
        type = type
    )
}

fun CareerGoal.toEntity(): GoalEntity {
    return GoalEntity(
        id = id,
        title = title,
        description = description,
        targetValue = targetValue,
        currentValue = currentValue,
        unit = unit,
        deadline = deadline,
        isCompleted = isCompleted,
        type = type
    )
}
