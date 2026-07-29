package com.bangersoul.aivance.feature.profile.domain

data class CareerRoadmap(
    val id: Long,
    val targetRole: String,
    val currentSkills: String,
    val steps: List<RoadmapStep>
)
