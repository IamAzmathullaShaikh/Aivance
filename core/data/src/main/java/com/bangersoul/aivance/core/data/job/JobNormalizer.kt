package com.bangersoul.aivance.core.data.job

import com.bangersoul.aivance.core.common.enums.EmploymentType
import com.bangersoul.aivance.core.common.enums.ExperienceLevel
import com.bangersoul.aivance.core.common.enums.RemoteType
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.database.model.JobEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobNormalizer @Inject constructor() {

    fun normalize(
        providerId: String,
        rawListing: com.bangersoul.aivance.core.common.model.JobListing
    ): JobListing {
        // In a real implementation, we might use AI to normalize fields
        // or apply regex/lookup tables. For now, we perform basic normalization.
        return rawListing.copy(
            sourceProvider = providerId,
            remoteType = when {
                rawListing.isRemote -> RemoteType.REMOTE
                rawListing.location.lowercase().contains("remote") -> RemoteType.REMOTE
                rawListing.location.lowercase().contains("hybrid") -> RemoteType.HYBRID
                else -> RemoteType.ON_SITE
            }
        )
    }

    fun parseSalary(salaryRange: String?): Pair<Double?, Double?> {
        if (salaryRange.isNullOrBlank()) return null to null
        // Basic parser for common formats like "$100k - $150k"
        return try {
            val parts = salaryRange.replace("$", "").replace("k", "000").split("-")
            val min = parts.getOrNull(0)?.trim()?.toDoubleOrNull()
            val max = parts.getOrNull(1)?.trim()?.toDoubleOrNull()
            min to max
        } catch (e: Exception) {
            null to null
        }
    }
}
