package com.bangersoul.aivance.core.enrichment.hunter

import com.bangersoul.aivance.core.common.model.Company
import com.bangersoul.aivance.core.common.model.Recruiter
import com.bangersoul.aivance.core.common.model.RecruiterContact
import com.bangersoul.aivance.core.common.result.ProviderError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.enrichment.hunter.dto.HunterEmailDto
import com.bangersoul.aivance.sdk.api.EnrichmentProvider
import com.bangersoul.aivance.sdk.config.ProviderConfiguration
import com.bangersoul.aivance.sdk.core.ConfigField
import com.bangersoul.aivance.sdk.core.FieldType
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.core.ProviderMetadata
import com.bangersoul.aivance.sdk.core.ProviderStatus
import com.bangersoul.aivance.sdk.core.ProviderType
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import timber.log.Timber

class HunterEnrichmentProvider(
    private var config: ProviderConfiguration,
    baseOkHttpClient: OkHttpClient,
    baseRetrofit: Retrofit
) : EnrichmentProvider(
    metadata = ProviderMetadata(
        id = "hunter",
        name = "Hunter.io",
        type = ProviderType.ENRICHMENT,
        version = "1.0.0",
        description = "Find verified email addresses for companies.",
        author = "BangerSoul",
        configFields = listOf(
            ConfigField(
                key = "apiKey",
                label = "Hunter API Key",
                isSensitive = true,
                fieldType = FieldType.PASSWORD,
                hint = "Get your key from hunter.io"
            )
        )
    ),
    capabilities = setOf(
        ProviderCapability.RecruiterDiscovery,
        ProviderCapability.EmailVerification
    )
) {
    override val isConfigured: Boolean
        get() = (config.secrets["apiKey"] ?: "").isNotBlank()

    override val hasCredentials: Boolean
        get() = isConfigured

    override suspend fun applyConfiguration(config: ProviderConfiguration) {
        this.config = config
    }

    private val apiKey: String
        get() = config.secrets["apiKey"] ?: ""

    private val retrofit: Retrofit by lazy {
        baseRetrofit.newBuilder()
            .baseUrl("https://api.hunter.io/")
            .build()
    }

    private val api: HunterApi by lazy { retrofit.create(HunterApi::class.java) }

    override suspend fun findRecruiters(domain: String): Result<List<Recruiter>> {
        if (apiKey.isBlank()) {
            return Result.Failure(ProviderError(metadata.id, message = "Hunter API Key not configured"))
        }
        return try {
            val response = api.domainSearch(domain, apiKey, limit = 10)
            if (!response.isSuccessful) {
                throw Exception("Hunter API failed: ${response.code()}")
            }
            val data = response.body()?.data ?: throw Exception("Hunter returned empty data")
            val emails = data.emails

            // Group emails by person (firstName + lastName) into Recruiter objects.
            val recruiters = emails.mapIndexedNotNull { index, email ->
                val name = when {
                    !email.firstName.isNullOrBlank() && !email.lastName.isNullOrBlank() ->
                        "${email.firstName} ${email.lastName}"
                    !email.firstName.isNullOrBlank() -> email.firstName!!
                    else -> null
                }
                val recruiterId = "${metadata.id}:${email.value ?: "email-$index"}"
                Recruiter(
                    id = recruiterId,
                    name = name ?: email.value?.substringBefore('@')?.takeIf { it.isNotBlank() } ?: "Recruiter",
                    companyId = domain,
                    title = email.position,
                    linkedinUrl = email.linkedin,
                    contacts = listOf(
                        RecruiterContact(
                            id = "${recruiterId}:contact",
                            recruiterId = recruiterId,
                            email = email.value ?: "",
                            confidence = email.confidence,
                            isVerified = email.confidence >= 90,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                )
            }
            Result.Success(recruiters)
        } catch (e: Exception) {
            Timber.w(e, "Hunter domain search failed for $domain")
            Result.Failure(ProviderError(metadata.id, message = e.message ?: "Hunter search failed", cause = e))
        }
    }

    override suspend fun verifyEmail(email: String): Result<Boolean> {
        if (apiKey.isBlank()) {
            return Result.Failure(ProviderError(metadata.id, message = "Hunter API Key not configured"))
        }
        return try {
            // Hunter enforces a 10 requests/second rate limit; be conservative.
            delay(110)
            val response = api.verifyEmail(email, apiKey)
            if (!response.isSuccessful) {
                throw Exception("Hunter API failed: ${response.code()}")
            }
            val data = response.body()?.data ?: throw Exception("Hunter returned empty data")
            val valid = data.status == "valid" && data.result != "undeliverable"
            Result.Success(valid)
        } catch (e: Exception) {
            Timber.w(e, "Hunter email verification failed for $email")
            Result.Failure(ProviderError(metadata.id, message = e.message ?: "Hunter verification failed", cause = e))
        }
    }

    override suspend fun enrichCompany(company: Company): Result<Company> {
        if (apiKey.isBlank()) {
            return Result.Failure(ProviderError(metadata.id, message = "Hunter API Key not configured"))
        }
        val domain = company.domain ?: company.websiteUrl?.substringAfter("://")?.substringBefore("/") ?: return Result.Success(company)
        return try {
            val response = api.domainSearch(domain, apiKey, limit = 1)
            if (!response.isSuccessful) {
                throw Exception("Hunter API failed: ${response.code()}")
            }
            val data = response.body()?.data ?: throw Exception("Hunter returned empty data")
            
            val enrichedCompany = company.copy(
                name = data.organization?.takeIf { it.isNotBlank() } ?: company.name
            )
            Result.Success(enrichedCompany)
        } catch (e: Exception) {
            Timber.w(e, "Hunter company enrichment failed for ${company.name}")
            Result.Failure(ProviderError(metadata.id, message = e.message ?: "Hunter enrichment failed", cause = e))
        }
    }

    override suspend fun onInitialize() {
        updateStatus(ProviderStatus.Ready)
    }

    override suspend fun onStart() {
        updateStatus(ProviderStatus.Active)
    }

    override suspend fun onStop() {
        updateStatus(ProviderStatus.Ready)
    }

    override suspend fun onDispose() {
        updateStatus(ProviderStatus.Disposed)
    }
}
