package com.bangersoul.aivance.job.mapper

import com.bangersoul.aivance.core.common.enums.EmploymentType
import com.bangersoul.aivance.core.common.enums.ExperienceLevel
import com.bangersoul.aivance.core.common.enums.RemoteType
import com.bangersoul.aivance.job.apify.dto.ApifyDatasetItem
import com.bangersoul.aivance.job.remoteok.dto.RemoteOKJobDto
import com.bangersoul.aivance.job.remotive.dto.RemotiveJobDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JobMapperTest {

    @Test
    fun `mapToJobListing from ApifyDatasetItem with all fields`() {
        val item = ApifyDatasetItem(
            id = "apify-1",
            title = "Senior Android Developer",
            company = "TechCorp",
            companyLogo = "https://techcorp.com/logo.png",
            location = "San Francisco, CA",
            salary = "$120k - $150k",
            description = "We need an Android expert.",
            descriptionHtml = "<p>We need an Android expert.</p>",
            url = "https://techcorp.com/jobs/1",
            postedAt = "2026-07-01T00:00:00.000Z",
            type = "full-time",
            experienceLevel = "senior",
            isRemote = true
        )

        val result = JobMapper.mapToJobListing(item, "apify-linkedin")

        assertEquals("apify-1", result.id)
        assertEquals("Senior Android Developer", result.title)
        assertEquals("TechCorp", result.company)
        assertEquals("https://techcorp.com/logo.png", result.companyLogoUrl)
        assertEquals("San Francisco, CA", result.location)
        assertEquals("$120k - $150k", result.salaryRange)
        assertEquals(120000.0, result.salaryMin!!, 0.001)
        assertEquals(150000.0, result.salaryMax!!, 0.001)
        assertEquals(EmploymentType.FULL_TIME, result.employmentType)
        assertEquals(ExperienceLevel.SENIOR_LEVEL, result.experienceLevel)
        assertEquals(RemoteType.REMOTE, result.remoteType)
        assertTrue(result.isRemote)
        assertEquals("We need an Android expert.", result.description)
        assertEquals("<p>We need an Android expert.</p>", result.descriptionHtml)
        assertEquals("https://techcorp.com/jobs/1", result.url)
        assertEquals("apify-linkedin", result.sourceProvider)
        assertEquals("USD", result.currency)
    }

    @Test
    fun `mapToJobListing from ApifyDatasetItem with null fields yields defaults`() {
        val item = ApifyDatasetItem()

        val result = JobMapper.mapToJobListing(item, "apify-test")

        assertNotNull(result.id)
        assertEquals("No Title", result.title)
        assertEquals("Unknown Company", result.company)
        assertEquals("Remote", result.location)
        assertNull(result.salaryRange)
        assertNull(result.salaryMin)
        assertNull(result.salaryMax)
        assertEquals(EmploymentType.FULL_TIME, result.employmentType)
        assertEquals(ExperienceLevel.NOT_SPECIFIED, result.experienceLevel)
        assertEquals(RemoteType.ON_SITE, result.remoteType)
        assertEquals(false, result.isRemote)
    }

    @Test
    fun `mapToJobListing from RemoteOKJobDto with all fields`() {
        val dto = RemoteOKJobDto(
            id = "rok-1",
            company = "StartupXYZ",
            companyLogo = "https://startup.xyz/logo.png",
            position = "Full Stack Developer",
            tags = listOf("react", "kotlin"),
            description = "Build the future.",
            location = "Remote (Global)",
            salaryMin = 80000.0,
            salaryMax = 120000.0,
            date = "2026-06-15",
            url = "https://remoteok.com/remote-jobs/full-stack-developer-1",
            legal = "No visa sponsorship"
        )

        val result = JobMapper.mapToJobListing(dto, "remoteok")

        assertEquals("rok-1", result.id)
        assertEquals("Full Stack Developer", result.title)
        assertEquals("StartupXYZ", result.company)
        assertEquals("https://startup.xyz/logo.png", result.companyLogoUrl)
        assertEquals("Remote (Global)", result.location)
        assertEquals(80000.0, result.salaryMin!!, 0.001)
        assertEquals(120000.0, result.salaryMax!!, 0.001)
        assertEquals(EmploymentType.FULL_TIME, result.employmentType)
        assertEquals(RemoteType.REMOTE, result.remoteType)
        assertTrue(result.isRemote)
        assertEquals("Build the future.", result.description)
        assertEquals("https://remoteok.com/remote-jobs/full-stack-developer-1", result.url)
        assertEquals("remoteok", result.sourceProvider)
    }

    @Test
    fun `mapToJobListing from RemoteOKJobDto with null id generates UUID`() {
        val dto = RemoteOKJobDto(position = "Dev", company = "Co")

        val result = JobMapper.mapToJobListing(dto, "remoteok")

        assertNotNull(result.id)
        assertTrue(result.id.isNotEmpty())
    }

    @Test
    fun `mapToJobListing from RemotiveJobDto with all fields`() {
        val dto = RemotiveJobDto(
            id = 42L,
            url = "https://remotive.com/job/42",
            title = "Backend Engineer",
            companyName = "DataFlow",
            companyLogo = "https://dataflow.io/logo.png",
            category = "Engineering",
            tags = listOf("python", "aws"),
            jobType = "full_time",
            publicationDate = "2026-07-10T14:30:00",
            candidateLocation = "Worldwide",
            salary = "$130k",
            description = "Backend role at DataFlow."
        )

        val result = JobMapper.mapToJobListing(dto, "remotive")

        assertEquals("42", result.id)
        assertEquals("Backend Engineer", result.title)
        assertEquals("DataFlow", result.company)
        assertEquals("https://dataflow.io/logo.png", result.companyLogoUrl)
        assertEquals("Worldwide", result.location)
        assertEquals(EmploymentType.FULL_TIME, result.employmentType)
        assertEquals(RemoteType.REMOTE, result.remoteType)
        assertTrue(result.isRemote)
        assertEquals("Backend role at DataFlow.", result.description)
        assertEquals("https://remotive.com/job/42", result.url)
        assertEquals("remotive", result.sourceProvider)
    }

    @Test
    fun `mapToJobListing from RemotiveJobDto with null fields`() {
        val dto = RemotiveJobDto()

        val result = JobMapper.mapToJobListing(dto, "remotive")

        assertNotNull(result.id)
        assertEquals("No Title", result.title)
        assertEquals("Unknown Company", result.company)
        assertEquals("Remote", result.location)
        assertEquals(EmploymentType.FULL_TIME, result.employmentType)
        assertEquals(RemoteType.REMOTE, result.remoteType)
        assertTrue(result.isRemote)
    }

    @Test
    fun `parseSalary extracts min correctly`() {
        assertEquals(100000.0, JobMapper.parseSalary("$100k - $150k", true)!!, 0.001)
        assertEquals(80.0, JobMapper.parseSalary("$80 - $120", true)!!, 0.001)
        assertEquals(100000.0, JobMapper.parseSalary("100k - 150k", true)!!, 0.001)
        assertNull(JobMapper.parseSalary(null, true))
        assertNull(JobMapper.parseSalary("Negotiable", true))
    }

    @Test
    fun `parseSalary extracts max correctly`() {
        assertEquals(150000.0, JobMapper.parseSalary("$100k - $150k", false)!!, 0.001)
        assertEquals(120.0, JobMapper.parseSalary("$80 - $120", false)!!, 0.001)
        assertEquals(150000.0, JobMapper.parseSalary("100k - 150k", false)!!, 0.001)
        assertNull(JobMapper.parseSalary(null, false))
    }

    @Test
    fun `parseSalary handles k suffix`() {
        assertEquals(100000.0, JobMapper.parseSalary("100k", true)!!, 0.001)
        assertEquals(200000.0, JobMapper.parseSalary("200K", true)!!, 0.001)
    }

    @Test
    fun `parseDate handles multiple formats`() {
        assertTrue("ISO8601 timestamp should be positive", JobMapper.parseDate("2026-07-01T00:00:00.000Z") > 0)
        assertTrue("Simple date should be positive", JobMapper.parseDate("2026-06-15") > 0)
        assertTrue("Date time should be positive", JobMapper.parseDate("2026-07-10T14:30:00") > 0)
    }

    @Test
    fun `parseDate returns valid timestamp for null or invalid input`() {
        val result = JobMapper.parseDate(null)
        assertTrue("Expected valid timestamp for null, got $result", result > 0 && result < 2000000000000)

        val invalidResult = JobMapper.parseDate("not-a-date")
        assertTrue("Expected valid timestamp for invalid date, got $invalidResult", invalidResult > 0 && invalidResult < 2000000000000)
    }
}
