# Implementation Plan: Phase 4 - Repository Layer

This plan outlines the complete implementation of the Repository Layer for AiVance. We will transition from simple DAO wrappers to a robust, cache-aware, paging-enabled, and sync-ready data layer following Clean Architecture.

## User Review Required

> [!IMPORTANT]
> - We will introduce `core:data` and `core:domain` modules to host cross-cutting repository logic and cache abstractions.
> - We will use Paging 3 for large lists like Jobs and Tracker history.
> - Data sources will be abstracted into `LocalDataSource` interfaces to isolate Room/DataStore logic.
> - Existing repository interfaces and implementations will be moved from feature modules to `core:domain` and `core:data` respectively to centralize data logic.

## Proposed Changes

### 1. New Core Modules
- [NEW] `core:domain`: Centralized domain models (shared) and repository interfaces.
- [NEW] `core:data`: Centralized data implementation logic, caching, and common local data sources.

### 2. Domain Layer (`core:domain`)
- [MODIFY] Move `DomainModels.kt` from `core:common` to `core:domain`.
- [NEW] Define unified Repository interfaces:
  - `AtsRepository`
  - `ResumeRepository`
  - `JobRepository`
  - `CoverLetterRepository`
  - `InterviewRepository`
  - `SettingsRepository`
  - `ProfileRepository`
  - `TrackerRepository`
- [MODIFY] Move/Reconcile repository interfaces from feature modules to `core:domain`.

### 3. Cache Abstraction (`core:data`)
- [NEW] `CacheManager<K, V>`: Memory-based cache with Time-to-Live (TTL) and eviction policies.
- [NEW] `DiskCache`: Room-backed or file-backed simple cache for larger data blobs (e.g., parsed resume text).

### 4. Local Data Sources (`core:data`)
- [NEW] Implement `xxxLocalDataSource` interfaces and their implementations for each feature:
  - `ResumeLocalDataSourceImpl` (Room)
  - `JobLocalDataSourceImpl` (Room)
  - `SettingsLocalDataSourceImpl` (DataStore)
  - `UserLocalDataSourceImpl` (Room/DataStore)
  - `AnalyticsLocalDataSourceImpl` (Room)

### 5. Repository Implementations (`core:data`)
- [NEW] Implement repository classes coordinating `LocalDataSources` and `CacheManager`.
- [MODIFY] Move/Refactor `RepositoryImpl` classes from feature modules to `core:data`.
- [MODIFY] Update Mappers to handle Entity <-> Domain conversions within the repository layer.
- [MODIFY] Ensure all repository methods return `CoreResult` or `Flow<CoreResult>`.

### 6. Paging Integration (`core:data`)
- [NEW] `JobPagingSource`: Room-backed `PagingSource` for efficient job listings.
- [NEW] `TrackerPagingSource`: Room-backed `PagingSource` for application history.

### 7. Synchronisation Metadata
- [MODIFY] Update Room Entities in `core:database` to include:
  - `syncState`: Enum (SYNCED, PENDING_UPDATE, PENDING_DELETE)
  - `lastUpdated`: Long (Timestamp)
  - `isDeleted`: Boolean (Soft delete flag)
- [MODIFY] Update Repositories to handle these flags during CRUD operations.

### 8. Dependency Injection (`core:data`)
- [NEW] `DataModule` in `core:data` to provide `CacheManager`, `LocalDataSources`, and bind `Repositories` to their interfaces in `core:domain`.

## Verification Plan

### Automated Tests
- **Repository Unit Tests**: Use `MockK` to mock `LocalDataSources` and `CacheManager`. Verify coordination logic and mapping.
- **CacheManager Tests**: Verify TTL expiration and eviction logic.
- **Paging Source Tests**: Verify `PagingSource` returns correct data chunks and handles invalidation.
- **Sync Metadata Tests**: Verify CRUD operations correctly update `lastUpdated` and `syncState`.

### Manual Verification
- **Data Flow Verification**: Verify that existing UI screens (Dashboard, Jobs, Resume) still display data correctly after moving to the centralized repository layer.
- **Performance Check**: Verify that large lists (Jobs, Tracker) scroll smoothly using the new Paging 3 implementation.
- **Cache Hit Verification**: Use logs to verify that `CacheManager` correctly serves data from memory when within TTL.
