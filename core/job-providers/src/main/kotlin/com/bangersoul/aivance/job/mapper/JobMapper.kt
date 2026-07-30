package com.bangersoul.aivance.job.mapper

import com.bangersoul.aivance.core.common.enums.EmploymentType
import com.bangersoul.aivance.core.common.enums.ExperienceLevel
import com.bangersoul.aivance.core.common.enums.RemoteType
import com.bangersoul.aivance.core.common.enums.JobType
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.job.apify.dto.ApifyDatasetItem
import com.bangersoul.aivance.job.remoteok.dto.RemoteOKJobDto
import com.bangersoul.aivance.job.remotive.dto.RemotiveJobDto
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

object JobMapper {
    private val dateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
    )

    fun mapToJobListing(item: ApifyDatasetItem, providerId: String): JobListing {
        return JobListing(
            id = item.id ?: UUID.randomUUID().toString(),
            title = item.title ?: "No Title",
            company = item.company ?: "Unknown Company",
            companyLogoUrl = item.companyLogo,
            location = item.location ?: "Remote",
            salaryRange = item.salary,
            salaryMin = parseSalary(item.salary, true),
            salaryMax = parseSalary(item.salary, false),
            currency = "USD",
            employmentType = parseEmploymentType(item.type),
            experienceLevel = parseExperienceLevel(item.experienceLevel),
            remoteType = if (item.isRemote == true) RemoteType.REMOTE else RemoteType.ON_SITE,
            jobType = mapToJobType(parseEmploymentType(item.type)),
            isRemote = item.isRemote ?: false,
            description = item.description ?: "",
            descriptionHtml = item.descriptionHtml,
            url = item.url ?: "",
            sourceProvider = providerId,
            postedDate = parseDate(item.postedAt)
        )
    }

    fun mapToJobListing(dto: RemoteOKJobDto, providerId: String): JobListing {
        return JobListing(
            id = dto.id ?: UUID.randomUUID().toString(),
            title = dto.position ?: "No Title",
            company = dto.company ?: "Unknown Company",
            companyLogoUrl = dto.companyLogo,
            location = dto.location ?: "Remote",
            salaryMin = dto.salaryMin,
            salaryMax = dto.salaryMax,
            currency = "USD",
            employmentType = EmploymentType.FULL_TIME,
            remoteType = RemoteType.REMOTE,
            isRemote = true,
            description = dto.description ?: "",
            url = dto.url ?: "",
            sourceProvider = providerId,
            postedDate = parseDate(dto.date)
        )
    }

    fun mapToJobListing(dto: RemotiveJobDto, providerId: String): JobListing {
        return JobListing(
            id = dto.id?.toString() ?: UUID.randomUUID().toString(),
            title = dto.title ?: "No Title",
            company = dto.companyName ?: "Unknown Company",
            companyLogoUrl = dto.companyLogo,
            location = dto.candidateLocation ?: "Remote",
            employmentType = parseEmploymentType(dto.jobType),
            remoteType = RemoteType.REMOTE,
            isRemote = true,
            description = dto.description ?: "",
            url = dto.url ?: "",
            sourceProvider = providerId,
            postedDate = parseDate(dto.publicationDate)
        )
    }

    internal fun parseSalary(salary: String?, min: Boolean): Double? {
        if (salary == null) return null
        // Extract numbers, potentially with 'k' suffix
        val regex = Regex("""(\d+)[kK]?""")
        val matches = regex.findAll(salary).map { match ->
            val value = match.groupValues[1].toDouble()
            if (match.value.contains("k", ignoreCase = true)) value * 1000 else value
        }.toList()
        
        return if (min) {
            matches.firstOrNull()
        } else {
            matches.lastOrNull()
        }
    }

    internal fun parseDate(dateStr: String?): Long {
        if (dateStr == null) return System.currentTimeMillis()
        for (format in dateFormats) {
            try {
                return format.parse(dateStr)?.time ?: continue
            } catch (e: Exception) {
                // Continue to next format
            }
        }
        return System.currentTimeMillis()
    }

    private fun parseEmploymentType(type: String?): EmploymentType {
        return when (type?.lowercase()?.replace("_", "-")?.replace(" ", "")) {
            "full-time", "fulltime" -> EmploymentType.FULL_TIME
            "part-time", "parttime" -> EmploymentType.PART_TIME
            "contract" -> EmploymentType.CONTRACT
            "internship" -> EmploymentType.INTERNSHIP
            "freelance" -> EmploymentType.FREELANCE
            else -> EmploymentType.FULL_TIME
        }
    }

    private fun mapToJobType(employmentType: EmploymentType): JobType {
        return when (employmentType) {
            EmploymentType.FULL_TIME -> JobType.FULL_TIME
            EmploymentType.PART_TIME -> JobType.PART_TIME
            EmploymentType.CONTRACT -> JobType.CONTRACT
            EmploymentType.INTERNSHIP -> JobType.INTERNSHIP
            EmploymentType.FREELANCE -> JobType.FREELANCE
            else -> JobType.FULL_TIME
        }
    }

    private fun parseExperienceLevel(level: String?): ExperienceLevel {
        return when (level?.lowercase()) {
            "entry", "junior" -> ExperienceLevel.ENTRY_LEVEL
            "mid", "intermediate" -> ExperienceLevel.MID_LEVEL
            "senior" -> ExperienceLevel.SENIOR_LEVEL
            "lead", "manager" -> ExperienceLevel.EXECUTIVE
            else -> ExperienceLevel.NOT_SPECIFIED
        }
    }
}
