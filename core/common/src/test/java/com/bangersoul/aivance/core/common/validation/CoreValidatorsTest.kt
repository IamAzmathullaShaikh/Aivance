package com.bangersoul.aivance.core.common.validation

import com.bangersoul.aivance.core.common.model.AiProviderConfig
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.Resume
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreValidatorsTest {

    @Test
    fun emailValidator_validAndInvalid_returnsExpectedResult() {
        assertTrue(EmailValidator.validate("test@example.com").isValid)
        assertTrue(EmailValidator.validate("user.name+tag@sub.domain.co.uk").isValid)

        assertFalse(EmailValidator.validate("invalid-email").isValid)
        assertFalse(EmailValidator.validate("test@.com").isValid)
        assertFalse(EmailValidator.validate("").isValid)
    }

    @Test
    fun phoneValidator_validAndInvalid_returnsExpectedResult() {
        assertTrue(PhoneValidator.validate("+12345678901").isValid)
        assertTrue(PhoneValidator.validate("1234567890").isValid)
        assertTrue(PhoneValidator.validate("").isValid) // Optional phone

        assertFalse(PhoneValidator.validate("123").isValid)
        assertFalse(PhoneValidator.validate("abc-def-ghij").isValid)
    }

    @Test
    fun passwordValidator_lengthCheck_returnsExpectedResult() {
        assertTrue(PasswordValidator.validate("password123").isValid)
        assertFalse(PasswordValidator.validate("short").isValid)
    }

    @Test
    fun urlValidator_validAndInvalid_returnsExpectedResult() {
        assertTrue(UrlValidator.validate("https://example.com/job/123").isValid)
        assertTrue(UrlValidator.validate("http://domain.org").isValid)

        assertFalse(UrlValidator.validate("not-a-url").isValid)
        assertFalse(UrlValidator.validate("ftp://").isValid)
    }

    @Test
    fun jsonValidator_validAndInvalid_returnsExpectedResult() {
        assertTrue(JsonValidator.validate("""{"key": "value", "count": 10}""").isValid)
        assertTrue(JsonValidator.validate("""[1, 2, 3]""").isValid)

        assertFalse(JsonValidator.validate("{invalid json}").isValid)
    }

    @Test
    fun stringValidator_notBlankMinMaxAndRegex_returnsExpectedResult() {
        val notBlank = StringValidator.notBlank("Title")
        assertTrue(notBlank.validate("Software Engineer").isValid)
        assertFalse(notBlank.validate("   ").isValid)

        val minLen = StringValidator.minLength(5, "Bio")
        assertTrue(minLen.validate("Hello World").isValid)
        assertFalse(minLen.validate("Hi").isValid)

        val maxLen = StringValidator.maxLength(10, "Code")
        assertTrue(maxLen.validate("123456789").isValid)
        assertFalse(maxLen.validate("1234567890123").isValid)
    }

    @Test
    fun validatorComposition_and_combinesErrorsCorrectly() {
        val combined = StringValidator.notBlank("Text") and StringValidator.minLength(5, "Text")
        assertTrue(combined.validate("Valid text").isValid)

        val result = combined.validate("")
        assertTrue(result.isInvalid)
        assertEquals(2, (result as ValidationResult.Invalid).errors.size)
    }

    @Test
    fun resumeValidator_validAndInvalid_returnsExpectedResult() {
        val validResume = Resume(
            name = "John Doe",
            fileName = "John_Doe_Resume.pdf",
            fileUri = "content://com.bangersoul.aivance/resume.pdf",
            rawText = "Experienced Senior Android Developer with 8 years of Kotlin, Compose, Clean Architecture, and Hilt expertise."
        )
        assertTrue(ResumeValidator.validate(validResume).isValid)

        val invalidResume = Resume(
            name = "",
            fileName = "",
            fileUri = "",
            rawText = "Short"
        )
        val result = ResumeValidator.validate(invalidResume)
        assertTrue(result.isInvalid)
        assertEquals(3, (result as ValidationResult.Invalid).errors.size)
    }

    @Test
    fun jobValidator_validAndInvalid_returnsExpectedResult() {
        val validJob = JobListing(
            id = "job_101",
            title = "Staff Android Engineer",
            company = "Tech Corp",
            location = "Remote",
            description = "Develop cutting-edge Android apps",
            url = "https://techcorp.com/careers/101",
            sourceProvider = "APIFY"
        )
        assertTrue(JobValidator.validate(validJob).isValid)

        val invalidJob = JobListing(
            id = "",
            title = "",
            company = "",
            description = "",
            url = "invalid-url",
            sourceProvider = "APIFY"
        )
        val result = JobValidator.validate(invalidJob)
        assertTrue(result.isInvalid)
        assertEquals(4, (result as ValidationResult.Invalid).errors.size)
    }

    @Test
    fun providerValidator_knownAndUnknown_returnsExpectedResult() {
        assertTrue(ProviderValidator.validate("GEMINI").isValid)
        assertTrue(ProviderValidator.validate("openai").isValid)
        assertTrue(ProviderValidator.validate("Groq").isValid)

        assertFalse(ProviderValidator.validate("UNKNOWN_PROVIDER").isValid)
    }

    @Test
    fun configurationValidator_validAndInvalid_returnsExpectedResult() {
        val validConfig = AiProviderConfig(
            providerId = "GEMINI",
            apiKey = "AIzaSyDummyKey123",
            selectedModel = "GEMINI_1_5_FLASH",
            temperature = 0.7f,
            maxTokens = 2048
        )
        assertTrue(ConfigurationValidator.validate(validConfig).isValid)

        val invalidConfig = AiProviderConfig(
            providerId = "GEMINI",
            apiKey = "",
            selectedModel = "GEMINI_1_5_FLASH",
            temperature = 1.5f,
            maxTokens = -10
        )
        val result = ConfigurationValidator.validate(invalidConfig)
        assertTrue(result.isInvalid)
        assertEquals(3, (result as ValidationResult.Invalid).errors.size)
    }
}
