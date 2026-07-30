package com.bangersoul.aivance.core.common.exception

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreExceptionsTest {

    @Test
    fun domainException_properties_areSetCorrectly() {
        val cause = IllegalArgumentException("Invalid state")
        val ex = DomainException(
            errorCode = "DOMAIN_INVALID",
            message = "Domain operation failed",
            cause = cause,
            recoverable = false,
            metadata = mapOf("key" to "val")
        )

        assertEquals("DOMAIN_INVALID", ex.errorCode)
        assertEquals("Domain operation failed", ex.message)
        assertEquals(cause, ex.cause)
        assertFalse(ex.recoverable)
        assertEquals(mapOf("key" to "val"), ex.metadata)
    }

    @Test
    fun validationException_fieldAndRecoverable_areSetCorrectly() {
        val ex = ValidationException(
            field = "email",
            message = "Invalid format"
        )

        assertEquals("email", ex.field)
        assertEquals("VALIDATION_ERROR", ex.errorCode)
        assertTrue(ex.recoverable)
    }

    @Test
    fun providerException_providerIdAndStatusCode_areSetCorrectly() {
        val ex = ProviderException(
            providerId = "GEMINI",
            statusCode = 429,
            message = "Rate limit hit"
        )

        assertEquals("GEMINI", ex.providerId)
        assertEquals(429, ex.statusCode)
        assertTrue(ex.recoverable)
    }

    @Test
    fun rateLimitException_retryAfterSeconds_isSetCorrectly() {
        val ex = RateLimitException(retryAfterSeconds = 30L)

        assertEquals(30L, ex.retryAfterSeconds)
        assertEquals("RATE_LIMIT_EXCEEDED", ex.errorCode)
        assertTrue(ex.recoverable)
    }

    @Test
    fun allExceptions_instantiateWithoutErrors() {
        val exceptions: List<BaseException> = listOf(
            DomainException(message = "Domain error"),
            ValidationException(message = "Validation error"),
            ProviderException(providerId = "OPENAI", message = "Provider error"),
            RepositoryException(message = "Repository error"),
            NetworkException(message = "Network error"),
            DatabaseException(message = "Database error"),
            SecurityException(message = "Security error"),
            SerializationException(message = "Serialization error"),
            AuthenticationException(),
            RateLimitException(),
            QuotaExceededException(),
            UnknownException()
        )

        assertEquals(12, exceptions.size)
        exceptions.forEach { ex ->
            assertTrue(ex.message.isNotBlank())
            assertTrue(ex.errorCode.isNotBlank())
        }
    }
}
