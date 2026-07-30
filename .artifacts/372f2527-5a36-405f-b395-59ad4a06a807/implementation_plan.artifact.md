# Implementation Plan: Phase 3 - Persistence Layer

This plan outlines the implementation of the Aivance Persistence Layer using Room. It includes entities for all domain models, relationship mappings, DAOs, TypeConverters, and comprehensive testing.

## User Review Required

> [!IMPORTANT]
> - Database version will be incremented to 5.
> - **Schema Evolution:** We will introduce a more comprehensive schema covering Resumes, Interviews, AI Conversations, and Analytics.
> - **JSON Serialization:** TypeConverters will be used for complex nested collections (e.g., `List<ResumeSection>`) to avoid over-normalizing the database for data that is mostly read/written as a whole.
> - **Foreign Keys:** Cascade deletes will be implemented for child entities (e.g., `InterviewMessage` -> `InterviewSession`).

## Proposed Changes

### Core Database Module (`:core:database`)

#### [MODIFY] [AivanceDatabase.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/database/src/main/java/com/bangersoul/aivance/core/database/AivanceDatabase.kt)
- Update database version to 5.
- Add new entities to the `entities` list.
- Register `AivanceConverters`.
- Add new abstract DAO provider methods.
- Implement `MIGRATION_4_5`.

#### [NEW] [AivanceConverters.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/database/src/main/java/com/bangersoul/aivance/core/database/util/AivanceConverters.kt)
- Centralized `TypeConverters` for:
    - `List<String>` (JSON).
    - `List<ResumeSection>` (JSON).
    - `InterviewFeedback` (JSON).
    - Domain Enums (using string names).
    - `Map<String, String>` for Analytics properties.

#### [NEW] Entities (`:core:database/model`)
- **[NEW] [ResumeEntity.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/database/src/main/java/com/bangersoul/aivance/core/database/model/ResumeEntity.kt)**: Persisting resume metadata and content.
- **[NEW] [ResumeAnalysisEntity.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/database/src/main/java/com/bangersoul/aivance/core/database/model/ResumeAnalysisEntity.kt)**: Link analysis results to resumes.
- **[NEW] [InterviewSessionEntity.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/database/src/main/java/com/bangersoul/aivance/core/database/model/InterviewSessionEntity.kt)**: Core interview tracking.
- **[NEW] [InterviewMessageEntity.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/database/src/main/java/com/bangersoul/aivance/core/database/model/InterviewMessageEntity.kt)**: Individual messages within an interview.
- **[NEW] [AiConversationEntity.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/database/src/main/java/com/bangersoul/aivance/core/database/model/AiConversationEntity.kt)**: General AI chat persistence.
- **[NEW] [AiMessageEntity.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/database/src/main/java/com/bangersoul/aivance/core/database/model/AiMessageEntity.kt)**: Messages for general AI chat.
- **[NEW] [JobListingEntity.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/database/src/main/java/com/bangersoul/aivance/core/database/model/JobListingEntity.kt)**: Cached job search results.
- **[NEW] [UserProfileEntity.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/database/src/main/java/com/bangersoul/aivance/core/database/model/UserProfileEntity.kt)**: Local user settings and profile data.
- **[NEW] [AnalyticsEventEntity.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/database/src/main/java/com/bangersoul/aivance/core/database/model/AnalyticsEventEntity.kt)**: Offline event logging.
- **[MODIFY] [ApplicationEntity.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/database/src/main/java/com/bangersoul/aivance/core/database/model/ApplicationEntity.kt)**: Update schema if necessary to match domain model.

#### [NEW] DAOs (`:core:database/dao`)
- **[NEW] [ResumeDao.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/database/src/main/java/com/bangersoul/aivance/core/database/dao/ResumeDao.kt)**: Handle resume management.
- **[NEW] [InterviewDao.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/database/src/main/java/com/bangersoul/aivance/core/database/dao/InterviewDao.kt)**: Session and message operations.
- **[NEW] [AiDao.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/database/src/main/java/com/bangersoul/aivance/core/database/dao/AiDao.kt)**: Conversation management.
- **[NEW] [JobsDao.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/database/src/main/java/com/bangersoul/aivance/core/database/dao/JobsDao.kt)**: Job listing caching and application tracking.
- **[NEW] [ProfileDao.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/database/src/main/java/com/bangersoul/aivance/core/database/dao/ProfileDao.kt)**: Profile persistence.
- **[NEW] [AnalyticsDao.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/database/src/main/java/com/bangersoul/aivance/core/database/dao/AnalyticsDao.kt)**: Event logging and syncing.

#### [NEW] [DatabaseManager.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/database/src/main/java/com/bangersoul/aivance/core/database/util/DatabaseManager.kt)
- Provide a utility class for maintenance tasks:
    - `clearAllTables()`: For debug/reset purposes.
    - `checkpoint()`: Force a WAL checkpoint.
    - `getDatabaseSize()`: For analytics.

### Dependency Injection (:core:database)
- **[MODIFY] [DatabaseModule.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/database/src/main/java/com/bangersoul/aivance/core/database/di/DatabaseModule.kt)**: Provide the new DAOs and the `DatabaseManager`.

## Verification Plan

### Automated Tests
- **Unit Tests (`test` folder)**:
    - `AivanceConvertersTest`: Verify JSON serialization and Enum mappings.
- **Instrumented Tests (`androidTest` folder)**:
    - `DaoTests`: CRUD operations for all entities, including Flow observation tests.
    - `RelationshipTests`: Verify Foreign Key constraints (Cascade delete) and `@Relation` projections.
    - `DatabaseMigrationTest`: Use `MigrationTestHelper` to verify v4 -> v5 schema transition.

### Manual Verification
- **App Inspection**: Use Android Studio's Database Inspector to verify live data and schema structure.
- **Logs**: Verify that analytics events are being persisted before sync.
- **UI Interaction**: Confirm that changing data (e.g., completing a roadmap step) immediately updates the UI via Room's Flow support.
