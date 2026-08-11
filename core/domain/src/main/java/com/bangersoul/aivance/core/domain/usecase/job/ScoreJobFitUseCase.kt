package com.bangersoul.aivance.core.domain.usecase.job

import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.ProfileState
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.sdk.api.AIProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Request for LLM-assisted job fit scoring.
 *
 * @param jobs Listings to score (at most [ScoreJobFitUseCase.MAX_JOBS_PER_BATCH]
 *   are sent to the AI provider per call; the rest fall back to rule-based scoring).
 * @param profile The candidate profile used as the scoring context; when null no
 *   scoring happens and an empty map is returned.
 * @param query The active search query, used as extra scoring context when present.
 */
data class ScoreJobFitRequest(
    val jobs: List<JobListing>,
    val profile: ProfileState?,
    val query: String = ""
)

/**
 * LLM-assisted job fit scoring (R-04).
 *
 * Scores a batch of listings 0–100 against the user's [ProfileState] through the
 * best available AI provider, mirroring the ai-job-search fit-matrix workflow.
 *
 * **Graceful degradation:** this is a best-effort pipeline. When no AI provider
 * is configured, the provider call fails, or the response cannot be parsed, the
 * use case simply returns whatever scores it has (usually nothing) and callers
 * fall back to the deterministic rule-based scorer ([JobFitScorer] in
 * `feature:jobs`). It never throws and never blocks discovery on AI.
 *
 * **Caching:** results are cached per `(jobId, profile signature)` for the
 * lifetime of this singleton, so re-scoring the same listings for the same
 * profile (e.g. re-running a search, toggling the fit sort) is free and does
 * not burn provider quota.
 */
@Singleton
class ScoreJobFitUseCase @Inject constructor(
    private val providerManager: ProviderManager
) {

    private val cache = ConcurrentHashMap<String, Int>()

    /** Max listings sent to the AI provider per call — bounds token cost + latency. */
    private val maxJobsPerBatch = 10

    /** Description length sent per listing — enough signal, bounded tokens. */
    private val maxDescriptionChars = 300

    /**
     * Returns the AI-scored fit scores (job id → 0..100) for as many [ScoreJobFitRequest.jobs]
     * as could be scored — cached entries first, then one batched AI call for the rest.
     * Jobs that could not be scored (no provider, failure, parse miss) are simply
     * absent from the result.
     */
    suspend operator fun invoke(request: ScoreJobFitRequest): Map<String, Int> {
        val profile = request.profile ?: return emptyMap()
        val jobs = request.jobs.take(maxJobsPerBatch)
        if (jobs.isEmpty()) return emptyMap()

        val profileKey = profile.signature()
        val keyFor = { jobId: String -> "$jobId::$profileKey" }
        val uncached = jobs.filter { !cache.containsKey(keyFor(it.id)) }

        if (uncached.isNotEmpty()) {
            val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
            if (provider != null) {
                scoreWithProvider(provider, uncached, request, profileKey, keyFor)
            }
        }

        return jobs.mapNotNull { job ->
            cache[keyFor(job.id)]?.let { score -> job.id to score }
        }.toMap()
    }

    private suspend fun scoreWithProvider(
        provider: AIProvider,
        jobs: List<JobListing>,
        request: ScoreJobFitRequest,
        profileKey: String,
        keyFor: (String) -> String
    ) {
        try {
            val prompt = buildPrompt(jobs, request.profile ?: return, request.query)
            val response = provider.generateText(prompt).getOrNull() ?: return
            val allowedIds = jobs.map { it.id }.toSet()
            parseScores(response).forEach { (jobId, score) ->
                if (jobId in allowedIds) {
                    cache[keyFor(jobId)] = score.coerceIn(0, 100)
                }
            }
        } catch (_: Exception) {
            // Any AI failure degrades gracefully to the rule-based fallback.
        }
    }

    private fun buildPrompt(jobs: List<JobListing>, profile: ProfileState, query: String): String = buildString {
        appendLine("You are a job-fit scoring engine. Score how well each job matches the candidate profile.")
        appendLine("Return ONLY a JSON object mapping each job id to an integer fit score from 0 to 100 (higher = better fit).")
        appendLine("Weigh: role and experience match, skill overlap, and location/remote-work preference.")
        appendLine()
        appendLine("Candidate profile:")
        if (profile.targetRole.isNotBlank()) appendLine("- Target role: ${profile.targetRole}")
        if (profile.skills.isNotEmpty()) appendLine("- Skills: ${profile.skills.joinToString(", ")}")
        if (profile.workPreference.isNotBlank()) appendLine("- Work preference: ${profile.workPreference}")
        if (query.isNotBlank()) appendLine("- Active search query: $query")
        appendLine()
        appendLine("Jobs (id | title | company | location | description):")
        jobs.forEach { job ->
            val desc = job.description.replace('\n', ' ').take(maxDescriptionChars)
            appendLine("${job.id} | ${job.title} | ${job.company} | ${job.location} | $desc")
        }
    }

    /**
     * Extracts `"id": score` pairs from the provider response, tolerating
     * markdown JSON fences and extra fields the model may add.
     */
    private fun parseScores(response: String): Map<String, Int> {
        // Strip markdown fences when present (```json ... ```); any trailing
        // "json" language tag left in the body is ignored by the regex below.
        val body = if (response.contains("```")) {
            response.substringAfter("```").substringBeforeLast("```").trim()
        } else {
            response
        }
        return Regex("\"([^\"]+)\"\\s*:\\s*(-?\\d{1,3})")
            .findAll(body)
            .mapNotNull { match ->
                val id = match.groupValues[1].trim()
                val score = match.groupValues[2].toIntOrNull() ?: return@mapNotNull null
                if (id.isNotBlank()) id to score else null
            }
            .toMap()
    }

    private fun ProfileState.signature(): String =
        listOf(targetRole, skills.joinToString(","), workPreference, salaryExpectation)
            .joinToString("::")
            .lowercase()

    companion object {
        /** Visible for tests / documentation — the batch size is fixed by contract. */
        const val MAX_JOBS_PER_BATCH = 10
    }
}
