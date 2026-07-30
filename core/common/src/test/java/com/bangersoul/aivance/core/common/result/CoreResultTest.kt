package com.bangersoul.aivance.core.common.result

import com.bangersoul.aivance.core.common.exception.ValidationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreResultTest {

    @Test
    fun resultSuccess_mapAndFlatMap_transformValuesCorrectly() {
        val success: Result<Int> = Result.Success(10)
        assertTrue(success.isSuccess)
        assertFalse(success.isFailure)

        val mapped = success.map { it * 2 }
        assertEquals(20, (mapped as Result.Success).data)

        val flatMapped = success.flatMap { Result.Success("Number: $it") }
        assertEquals("Number: 10", (flatMapped as Result.Success).data)
    }

    @Test
    fun resultFailure_mapAndFlatMap_preserveError() {
        val error = DomainError("Operation failed")
        val failure: Result<Int> = Result.Failure(error)

        assertTrue(failure.isFailure)
        assertFalse(failure.isSuccess)

        val mapped = failure.map { it * 2 }
        assertTrue(mapped.isFailure)
        assertEquals(error, (mapped as Result.Failure).error)

        assertEquals("default", failure.getOrElse { "default" })
        assertNull(failure.getOrNull())
    }

    @Test
    fun either_leftAndRight_foldAndMapCorrectly() {
        val right: Either<String, Int> = Either.Right(42)
        assertTrue(right.isRight)
        assertFalse(right.isLeft)

        val mapped = right.map { it + 8 }
        assertEquals(50, mapped.getOrNull())

        val folded = right.fold(
            onLeft = { "Error: $it" },
            onRight = { "Success: $it" }
        )
        assertEquals("Success: 42", folded)

        val left: Either<String, Int> = Either.Left("Failed")
        assertTrue(left.isLeft)
        assertEquals("Fallback", left.getOrElse { "Fallback" })
    }

    @Test
    fun runCatchingCore_catchesBaseExceptionAndWrapsFailure() {
        val result = runCatchingCore {
            throw ValidationException(field = "username", message = "Username required")
        }

        assertTrue(result.isFailure)
        val failure = result as Result.Failure
        assertTrue(failure.error is ValidationError)
        assertEquals("username", (failure.error as ValidationError).field)
        assertEquals("Username required", failure.error.message)
    }

    @Test
    fun runCatchingCore_success_returnsSuccessResult() {
        val result = runCatchingCore { "Success Value" }
        assertTrue(result.isSuccess)
        assertEquals("Success Value", result.getOrNull())
    }
}
