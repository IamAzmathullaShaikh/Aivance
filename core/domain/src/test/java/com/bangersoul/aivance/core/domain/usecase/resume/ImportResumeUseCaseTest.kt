package com.bangersoul.aivance.core.domain.usecase.resume

import android.net.Uri
import com.bangersoul.aivance.core.common.result.DatabaseError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ImportResumeUseCaseTest {

    private lateinit var resumeRepository: ResumeRepository
    private lateinit var useCase: ImportResumeUseCase

    @Before
    fun setUp() {
        resumeRepository = mockk()
        useCase = ImportResumeUseCase(resumeRepository)
    }

    @Test
    fun `should import resume and return new resume id`() = runTest {
        coEvery { resumeRepository.importResume(any()) } returns Result.Success(42L)

        val result = useCase(mockk<Uri>())

        assertTrue(result.isSuccess)
        assertEquals(42L, (result as Result.Success).data)
        coVerify { resumeRepository.importResume(any()) }
    }

    @Test
    fun `should propagate repository failure`() = runTest {
        coEvery { resumeRepository.importResume(any()) } returns Result.Failure(DatabaseError("Import failed"))

        val result = useCase(mockk<Uri>())

        assertTrue(result.isFailure)
    }
}
