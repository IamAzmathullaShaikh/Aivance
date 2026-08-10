package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.model.Application
import com.bangersoul.aivance.core.common.model.InterviewFeedback
import com.bangersoul.aivance.core.common.model.InterviewSession
import com.bangersoul.aivance.core.common.model.Recruiter
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.database.dao.AnalyticsDao
import com.bangersoul.aivance.core.database.dao.AtsDao
import com.bangersoul.aivance.core.database.model.AnalyticsSnapshotEntity
import com.bangersoul.aivance.core.database.model.ResumeAnalysisEntity
import com.bangersoul.aivance.core.domain.analytics.CareerForecastEngine
import com.bangersoul.aivance.core.domain.analytics.CareerIntelligenceEngine
import com.bangersoul.aivance.core.domain.analytics.CareerScoreEngine
import com.bangersoul.aivance.core.domain.analytics.KPIEngine
import com.bangersoul.aivance.core.domain.analytics.RecommendationEngine
import com.bangersoul.aivance.core.domain.repository.ApplicationWorkflowRepository
import com.bangersoul.aivance.core.domain.repository.InterviewRepository
import com.bangersoul.aivance.core.domain.repository.UserRepository
import com.bangersoul.aivance.core.domain.repository.crm.RecruiterIntelligenceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Proves the M-03/P2-01 data-layer guarantee: [AnalyticsRepositoryImpl.getSnapshots]
 * self-heals an empty history with a real baseline snapshot derived from
 * actual applications, interview sessions and ATS results — never fabricated.
 */
class AnalyticsRepositoryImplTest {

    private val analyticsDao: AnalyticsDao = mockk()
    private val atsDao: AtsDao = mockk()
    private val workflowRepository: ApplicationWorkflowRepository = mockk()
    private val recruiterRepository: RecruiterIntelligenceRepository = mockk()
    private val userRepository: UserRepository = mockk()
    private val interviewRepository: InterviewRepository = mockk()
    private val recommendationEngine: RecommendationEngine = mockk()

    private lateinit var repository: AnalyticsRepositoryImpl

    /** Simulates Room: the DAO flow re-emits the current table contents. */
    private val snapshotEntities = MutableStateFlow<List<AnalyticsSnapshotEntity>>(emptyList())

    // Real data: 2 applications (1 reached INTERVIEWING), 1 session scored 85,
    // 1 legacy ATS analysis scored 80, 2 recruiters.
    private val atsEntity = ResumeAnalysisEntity(
        id = 1, resumeId = 100, jobDescription = "", score = 80,
        matchedKeywords = "", missingKeywords = "", feedback = "", date = 0L
    )
    private val appInterviewing = Application(id = 1, jobId = 10, currentStageId = "INTERVIEWING")
    private val appApplied = Application(id = 2, jobId = 10, currentStageId = "APPLIED")
    private val session = InterviewSession(
        id = "s1", targetRole = "Engineer", feedback = InterviewFeedback(overallScore = 85)
    )
    private val recruiter1 = Recruiter(id = "r1", name = "Ada", companyId = "10")
    private val recruiter2 = Recruiter(id = "r2", name = "Lin", companyId = "10")

    @Before
    fun setUp() {
        val kpiEngine = KPIEngine()
        val scoreEngine = CareerScoreEngine()
        repository = AnalyticsRepositoryImpl(
            analyticsDao = analyticsDao,
            atsDao = atsDao,
            workflowRepository = workflowRepository,
            recruiterRepository = recruiterRepository,
            userRepository = userRepository,
            interviewRepository = interviewRepository,
            kpiEngine = kpiEngine,
            scoreEngine = scoreEngine,
            intelEngine = CareerIntelligenceEngine(kpiEngine, scoreEngine),
            forecastEngine = CareerForecastEngine(),
            recommendationEngine = recommendationEngine
        )
        every { analyticsDao.getSnapshots() } returns snapshotEntities
        coEvery { analyticsDao.insertSnapshot(any()) } answers {
            snapshotEntities.value = snapshotEntities.value + (args[0] as AnalyticsSnapshotEntity)
            1L
        }
    }

    private fun stubRealData() {
        every { workflowRepository.getApplications() } returns
            flowOf(Result.Success(listOf(appInterviewing, appApplied)))
        every { interviewRepository.getSessions() } returns flowOf(Result.Success(listOf(session)))
        every { atsDao.getAtsResults() } returns flowOf(listOf(atsEntity))
        every { recruiterRepository.getRecruitersForCompany(any()) } returns
            flowOf(Result.Success(listOf(recruiter1, recruiter2)))
    }

    @Test
    fun `getSnapshots self-heals empty history with a real baseline snapshot`() = runTest {
        stubRealData()

        val result = repository.getSnapshots().first()

        assertTrue(result is Result.Success)
        val snapshots = (result as Result.Success).data
        assertEquals(1, snapshots.size)
        // interviewRate = 1 of 2 applied reached INTERVIEWING
        assertEquals(50.0, snapshots.first().kpis["interview_rate"])
        // (ats 80 + networking 10 + consistency 4 + readiness 85) / 4 = 44
        assertEquals(44, snapshots.first().careerScore)
        coVerify(exactly = 1) { analyticsDao.insertSnapshot(any()) }
    }

    @Test
    fun `baseline for a brand-new user derives an honest empty-state snapshot`() = runTest {
        every { workflowRepository.getApplications() } returns flowOf(Result.Success(emptyList()))
        every { interviewRepository.getSessions() } returns flowOf(Result.Success(emptyList()))
        every { atsDao.getAtsResults() } returns flowOf(emptyList())

        val result = repository.getSnapshots().first()

        assertTrue(result is Result.Success)
        val baseline = (result as Result.Success).data.single()
        assertEquals(0.0, baseline.kpis["interview_rate"])
        // (ats 0 + networking 0 + consistency 0 + default readiness 75) / 4 = 18
        assertEquals(18, baseline.careerScore)
    }

    @Test
    fun `getSnapshots does not insert a baseline when history exists`() = runTest {
        snapshotEntities.value = listOf(
            AnalyticsSnapshotEntity(id = 1L, kpiJson = "{}", careerScore = 60, dimensionScoresJson = "{}")
        )

        val result = repository.getSnapshots().first()

        assertTrue(result is Result.Success)
        assertEquals(1, (result as Result.Success).data.size)
        assertEquals(60, (result as Result.Success).data.first().careerScore)
        coVerify(exactly = 0) { analyticsDao.insertSnapshot(any()) }
    }

    @Test
    fun `repeated collection never double-inserts a baseline`() = runTest {
        stubRealData()

        repository.getSnapshots().first() // heals the empty history
        repository.getSnapshots().first() // history exists — must skip

        coVerify(exactly = 1) { analyticsDao.insertSnapshot(any()) }
    }

    @Test
    fun `createSnapshot persists derived kpis and career score from real data`() = runTest {
        stubRealData()

        val result = repository.createSnapshot()

        assertTrue(result is Result.Success)
        // The @Before stub mirrors Room: the insert lands in snapshotEntities.
        val persisted = snapshotEntities.value.single()
        assertEquals(44, persisted.careerScore)
        assertTrue(persisted.kpiJson.contains("\"interview_rate\":50.0"))
        assertTrue(persisted.dimensionScoresJson.contains("\"OVERALL\":44"))
    }
}
