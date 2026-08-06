package com.bangersoul.aivance.job.catalog

import kotlinx.serialization.Serializable

@Serializable
data class RemoteCompanyInfo(
    val name: String,
    val website: String,
    val remotePolicy: String, // "Fully Remote", "Remote-First", "Hybrid"
    val regions: List<String>,
    val technologies: List<String>,
    val companySize: String
)

object RemoteCompanyCatalog {
    val companies = listOf(
        RemoteCompanyInfo(
            name = "GitLab",
            website = "https://about.gitlab.com/jobs/",
            remotePolicy = "Fully Remote",
            regions = listOf("Global", "Americas", "EMEA", "APAC"),
            technologies = listOf("Ruby", "Go", "Vue.js", "Kubernetes", "PostgreSQL"),
            companySize = "1000+"
        ),
        RemoteCompanyInfo(
            name = "Automattic",
            website = "https://automattic.com/work-with-us/",
            remotePolicy = "Fully Remote",
            regions = listOf("Global"),
            technologies = listOf("PHP", "JavaScript", "React", "WordPress", "Mobile"),
            companySize = "1000+"
        ),
        RemoteCompanyInfo(
            name = "Zapier",
            website = "https://zapier.com/careers",
            remotePolicy = "Fully Remote",
            regions = listOf("Global", "Americas"),
            technologies = listOf("Python", "React", "AWS", "Django"),
            companySize = "500-1000"
        ),
        RemoteCompanyInfo(
            name = "Buffer",
            website = "https://buffer.com/journey",
            remotePolicy = "Fully Remote",
            regions = listOf("Global"),
            technologies = listOf("Node.js", "React", "Android", "iOS"),
            companySize = "50-200"
        ),
        RemoteCompanyInfo(
            name = "Doist",
            website = "https://doist.com/careers",
            remotePolicy = "Fully Remote",
            regions = listOf("Global"),
            technologies = listOf("Python", "Android", "iOS", "React", "Web"),
            companySize = "50-200"
        ),
        RemoteCompanyInfo(
            name = "DuckDuckGo",
            website = "https://duckduckgo.com/hiring",
            remotePolicy = "Fully Remote",
            regions = listOf("Global"),
            technologies = listOf("Perl", "Python", "React", "Privacy"),
            companySize = "200-500"
        )
    )

    fun findByName(name: String): RemoteCompanyInfo? {
        return companies.firstOrNull { it.name.equals(name, ignoreCase = true) || name.contains(it.name, ignoreCase = true) }
    }

    fun filterByTechnology(tech: String): List<RemoteCompanyInfo> {
        return companies.filter { c -> c.technologies.any { it.equals(tech, ignoreCase = true) } }
    }
}
