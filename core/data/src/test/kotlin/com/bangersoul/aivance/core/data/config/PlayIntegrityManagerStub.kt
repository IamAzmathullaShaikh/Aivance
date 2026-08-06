package com.bangersoul.aivance.core.data.config

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.config.PlayIntegrityManager

/**
 * Test double for [PlayIntegrityManager] that always reports success.
 *
 * Lives in the unit-test source set only — production binds the real Play
 * Integrity SDK implementation ([PlayIntegrityManagerImpl]).
 */
class PlayIntegrityManagerStub : PlayIntegrityManager {
    override suspend fun verifyIntegrity(): CoreResult<PlayIntegrityManager.IntegrityVerdict> {
        return Result.Success(
            PlayIntegrityManager.IntegrityVerdict(
                deviceIntegrity = true,
                appRecognitionVerdict = true
            )
        )
    }

    override suspend fun verifyAppIntegrity(): CoreResult<PlayIntegrityManager.AppIntegrityStatus> {
        return Result.Success(
            PlayIntegrityManager.AppIntegrityStatus(
                isGenuine = true,
                packageName = "com.bangersoul.aivance",
                versionCode = 1
            )
        )
    }
}
