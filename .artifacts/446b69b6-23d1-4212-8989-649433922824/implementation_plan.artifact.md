# Implementation Plan - Phase 7: Job Provider Layer

This plan outlines the implementation of a comprehensive, multi-platform Job Provider layer for AiVance, integrated through the Provider SDK.

## User Review Required

> [!IMPORTANT]
> - A new module `:core:job-providers` will be created to house all concrete implementations.
> - We will prioritize **Apify** for major platforms (LinkedIn, Indeed, etc.) to ensure reliability and avoid in-app scraping blocks.
> - Direct REST APIs (RemoteOK, Remotive) will be implemented natively using Retrofit.
> - "JobSpy" will be implemented as a documented interface/adapter, as the actual scraping logic is usually cloud-based or requires external scripts.

## Proposed Changes

### 1. Infrastructure Enhancement

#### [MODIFY] [JobProvider.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/sdk/src/main/kotlin/com/bangersoul/aivance/sdk/api/JobProvider.kt)
- Update `searchJobs` signature to accept `SearchFilter` instead of a simple query string.
- Add support for pagination (offset/limit) and sorting.
- Add `getCompanyDetails(companyId: String)` method.

#### [MODIFY] [DomainModels.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/common/src/main/java/com/bangersoul/aivance/core/common/model/DomainModels.kt)
- Update `JobListing` to include `logoUrl`, `salaryMin`, `salaryMax`, `currency`, and `fullDescription`.
- Update `SearchFilter` to include `salaryMin`, `salaryMax`, `experienceLevel`, and `remoteOnly`.

#### [MODIFY] [CoreDtos.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/common/src/main/java/com/bangersoul/aivance/core/common/dto/CoreDtos.kt)
- Sync `JobListingDto` with domain model changes.

---

### 2. New Module: `:core:job-providers`

#### [NEW] [build.gradle.kts](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/job-providers/build.gradle.kts)
- Define dependencies: `:core:sdk`, `:core:common`, `:core:network`, Hilt, Retrofit, Kotlinx Serialization.

#### [NEW] [RestJobProvider.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/job-providers/src/main/kotlin/com/bangersoul/aivance/jobproviders/base/RestJobProvider.kt)
- Base class for providers using standard REST APIs. Handles common Retrofit/OkHttp logic.

#### [NEW] [ApifyJobProvider.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/job-providers/src/main/kotlin/com/bangersoul/aivance/jobproviders/apify/ApifyJobProvider.kt)
- Generic implementation to interact with Apify Actors.

---

### 3. Platform-Specific Providers

#### [NEW] [RemoteOKProvider.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/job-providers/src/main/kotlin/com/bangersoul/aivance/jobproviders/remoteok/RemoteOKProvider.kt)
- Direct integration with RemoteOK API.

#### [NEW] [RemotiveProvider.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/job-providers/src/main/kotlin/com/bangersoul/aivance/jobproviders/remotive/RemotiveProvider.kt)
- Direct integration with Remotive API.

#### [NEW] [ LinkedInProvider.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/job-providers/src/main/kotlin/com/bangersoul/aivance/jobproviders/linkedin/LinkedInProvider.kt)
- Apify-powered implementation for LinkedIn.

#### [NEW] [IndeedProvider.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/job-providers/src/main/kotlin/com/bangersoul/aivance/jobproviders/indeed/IndeedProvider.kt)
- Apify-powered implementation for Indeed.

#### [NEW] [JobSpyProvider.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/job-providers/src/main/kotlin/com/bangersoul/aivance/jobproviders/jobspy/JobSpyProvider.kt)
- Adapter implementation for JobSpy schema.

---

### 4. Normalization & Mapping

#### [NEW] [JobMapper.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/job-providers/src/main/kotlin/com/bangersoul/aivance/jobproviders/mapper/JobMapper.kt)
- Centralized mapping logic to convert platform-specific DTOs to `JobListing`.
- Handles date parsing and salary normalization.

---

### 5. Dependency Injection

#### [NEW] [JobProvidersModule.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/job-providers/src/main/kotlin/com/bangersoul/aivance/jobproviders/di/JobProvidersModule.kt)
- Hilt module to provide `JobProvider` implementations and register them in `ProviderRegistry`.

---

### 6. Reliability & Performance

#### [NEW] [ProviderRateLimiter.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/job-providers/src/main/kotlin/com/bangersoul/aivance/jobproviders/util/ProviderRateLimiter.kt)
- Utility to handle rate limiting and exponential backoff.


## Verification Plan

### Automated Tests
- **Unit Tests**:
    - `JobMapperTest`: Test mapping logic with sample JSON from all providers.
    - `SearchFilterTest`: Test filter building logic.
- **Integration Tests**:
    - `ProviderIntegrationTest`: Use `MockWebServer` to verify API calls and error handling for `RemoteOK` and `Remotive`.

### Manual Verification
- Execute job searches through a diagnostic screen/CLI to verify results from multiple sources.
- Check logcat for correct telemetry (latency, success rates).
