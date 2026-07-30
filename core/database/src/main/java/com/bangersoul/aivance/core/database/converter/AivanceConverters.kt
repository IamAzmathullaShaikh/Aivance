package com.bangersoul.aivance.core.database.converter

import androidx.room.TypeConverter
import com.bangersoul.aivance.core.common.enums.AIModel
import com.bangersoul.aivance.core.common.model.AIMessage
import com.bangersoul.aivance.core.common.model.InterviewFeedback
import com.bangersoul.aivance.core.common.model.InterviewMessage
import com.bangersoul.aivance.core.common.model.ProviderCapability
import com.bangersoul.aivance.core.common.model.ResumeSection
import com.bangersoul.aivance.core.common.model.RoadmapStep
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class AivanceConverters {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toUUID(uuidString: String?): UUID? = uuidString?.let { UUID.fromString(it) }

    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun toInstant(millis: Long?): Instant? = millis?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? =
        dateTime?.atZone(ZoneOffset.UTC)?.toInstant()?.toString()

    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? =
        dateTimeString?.let { LocalDateTime.ofInstant(Instant.parse(it), ZoneOffset.UTC) }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? = value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toStringList(value: String?): List<String>? = value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun fromResumeSectionList(value: List<ResumeSection>?): String? =
        value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toResumeSectionList(value: String?): List<ResumeSection>? =
        value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun fromInterviewMessageList(value: List<InterviewMessage>?): String? =
        value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toInterviewMessageList(value: String?): List<InterviewMessage>? =
        value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun fromInterviewFeedback(value: InterviewFeedback?): String? =
        value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toInterviewFeedback(value: String?): InterviewFeedback? =
        value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun fromRoadmapStepList(value: List<RoadmapStep>?): String? =
        value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toRoadmapStepList(value: String?): List<RoadmapStep>? =
        value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun fromAIMessageList(value: List<AIMessage>?): String? =
        value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toAIMessageList(value: String?): List<AIMessage>? =
        value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun fromAIModelList(value: List<AIModel>?): String? =
        value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toAIModelList(value: String?): List<AIModel>? =
        value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun fromProviderCapabilityList(value: List<ProviderCapability>?): String? =
        value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toProviderCapabilityList(value: String?): List<ProviderCapability>? =
        value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? =
        value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? =
        value?.let { json.decodeFromString(it) }
}
