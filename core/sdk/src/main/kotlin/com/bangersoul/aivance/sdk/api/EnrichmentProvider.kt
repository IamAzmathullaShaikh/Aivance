package com.bangersoul.aivance.sdk.api

import com.bangersoul.aivance.core.common.model.Recruiter
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.sdk.core.BaseProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.core.ProviderMetadata

/**
 * Interface for enrichment providers that discover recruiters and verify contact details.
 */
abstract class EnrichmentProvider(
    metadata: ProviderMetadata,
    capabilities: Set<ProviderCapability>
) : BaseProvider(metadata, capabilities) {

    /**
     * Discovers recruiters associated with a given company domain.
     *
     * @param domain The company domain (e.g., "google.com").
     * @return Result containing a list of discovered recruiters.
     */
    abstract suspend fun findRecruiters(domain: String): Result<List<Recruiter>>

    /**
     * Verifies the validity of an email address.
     *
     * @param email The email to verify.
     * @return Result containing a boolean (true if valid) or a confidence score.
     */
    abstract suspend fun verifyEmail(email: String): Result<Boolean>
}
