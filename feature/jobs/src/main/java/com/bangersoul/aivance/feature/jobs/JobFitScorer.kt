package com.bangersoul.aivance.feature.jobs

import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.UserProfile

object JobFitScorer {

    /**
     * Calculates a 0-100 fit score based on target role, skills, location, and remote preferences.
     */
    fun calculateFitScore(job: JobListing, profile: UserProfile?): Int {
        if (profile == null) return 70 // default benchmark score

        var score = 50

        // 1. Role match (up to 25 pts)
        if (profile.targetRole.isNotBlank()) {
            val targetLower = profile.targetRole.lowercase()
            val jobTitleLower = job.title.lowercase()
            if (jobTitleLower.contains(targetLower) || targetLower.contains(jobTitleLower)) {
                score += 25
            } else {
                val keywords = targetLower.split(" ").filter { it.length > 3 }
                val matches = keywords.count { jobTitleLower.contains(it) }
                score += (matches * 10).coerceAtMost(20)
            }
        }

        // 2. Skill overlap (up to 15 pts)
        if (profile.skills.isNotEmpty()) {
            val descLower = (job.title + " " + job.description).lowercase()
            val matchedSkills = profile.skills.count { descLower.contains(it.lowercase()) }
            val ratio = matchedSkills.toDouble() / profile.skills.size.coerceAtLeast(1)
            score += (ratio * 15).toInt()
        }

        // 3. Remote / Location match (up to 10 pts)
        if (job.isRemote && profile.workPreference.equals("REMOTE", ignoreCase = true)) {
            score += 10
        } else if (profile.location.isNotBlank() && job.location.contains(profile.location, ignoreCase = true)) {
            score += 10
        }

        return score.coerceIn(10, 100)
    }

    /**
     * Filters out jobs matching excluded keywords (e.g., "unpaid", "commission-only", "senior" when looking for junior).
     */
    fun filterExcludedKeywords(jobs: List<JobListing>, excludedKeywords: List<String>): List<JobListing> {
        if (excludedKeywords.isEmpty()) return jobs
        val lowerExcluded = excludedKeywords.map { it.lowercase().trim() }.filter { it.isNotBlank() }
        return jobs.filter { job ->
            val text = (job.title + " " + job.description).lowercase()
            lowerExcluded.none { kw -> text.contains(kw) }
        }
    }
}
