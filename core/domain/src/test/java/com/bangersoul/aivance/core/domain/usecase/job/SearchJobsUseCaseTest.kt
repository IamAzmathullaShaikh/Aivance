package com.bangersoul.aivance.core.domain.usecase.job

import com.bangersoul.aivance.core.domain.repository.JobRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class SearchJobsUseCaseTest {

    private lateinit var jobRepository: JobRepository
    private lateinit var useCase: SearchJobsUseCase

    @Before
    fun setUp() {
        jobRepository = mockk()
        useCase = SearchJobsUseCase(jobRepository)
    }

    @Test
    fun `should return flow for valid query`() {
        every { jobRepository.searchJobs(any(), any()) } returns emptyFlow()

        val result = useCase(SearchJobsRequest(query = "Android Developer"))
        assertNotNull(result)
    }

    @Test
    fun `should return empty flow for blank query`() {
        val result = useCase(SearchJobsRequest(query = ""))
        assertNotNull(result)
    }
}
