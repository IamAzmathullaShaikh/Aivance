package com.bangersoul.aivance.core.domain.assistant

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.usecase.resume.AnalyseResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.AnalyseResumeRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CapabilityRouter @Inject constructor(
    private val analyseResumeUseCase: AnalyseResumeUseCase
) {
    suspend fun routeIntent(intent: String, params: Map<String, String>): CoreResult<String> = runCatchingCore {
        when (intent) {
            "ANALYZE_RESUME" -> {
                val rid = params["resumeId"]?.toLongOrNull() ?: 0L
                val vid = params["versionId"]?.toLongOrNull() ?: 0L
                val desc = params["jobDescription"] ?: ""
                analyseResumeUseCase(AnalyseResumeRequest(rid, vid, desc)).toString()
            }
            else -> "I understand your intent is $intent, but I cannot execute it yet."
        }
    }
}
