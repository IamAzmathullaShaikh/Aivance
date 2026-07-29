package com.bangersoul.aivance.feature.coverletter.domain.model

data class CoverLetter(
    val id: Int = 0,
    val company: String,
    val role: String,
    val content: String,
    val dateCreated: Long,
    val tone: LetterTone
)
