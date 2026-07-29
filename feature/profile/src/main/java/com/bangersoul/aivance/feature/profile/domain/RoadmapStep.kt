package com.bangersoul.aivance.feature.profile.domain

data class RoadmapStep(
    val id: Long,
    val title: String,
    val description: String,
    val order: Int,
    val isCompleted: Boolean
)
