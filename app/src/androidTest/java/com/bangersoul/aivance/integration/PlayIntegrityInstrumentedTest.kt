package com.bangersoul.aivance.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bangersoul.aivance.core.common.result.IntegrityError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.data.config.PlayIntegrityManagerImpl
import com.bangersoul.aivance.core.data.util.DefaultClock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds

/**
 * On-device validation of the real Play Integrity SDK path.
 *
 * Devices/emulators without Play Services or the Play Store exercise the
 * graceful-degradation path (a typed [IntegrityError] Failure); Play-equipped
 * images may return a real decoded token. Either outcome is valid — crashing
 * is not.
 *
 * Run with: ./gradlew connectedDebugAndroidTest  (device/emulator required)
 */
@RunWith(AndroidJUnit4::class)
class PlayIntegrityInstrumentedTest {

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun realIntegrityManager_returnsVerdictOrGracefulFailure() = runTest(timeout = 60.seconds) {
        val manager = PlayIntegrityManagerImpl(context, DefaultClock())

        val result = manager.verifyIntegrity()
        when (result) {
            is Result.Success -> {
                // Token requested and payload decoded — a real verdict was surfaced.
                assertNotNull("Verdict must be present on success", result.data)
            }
            is Result.Failure -> {
                assertTrue(
                    "Unexpected integrity error: ${result.error.message}",
                    result.error is IntegrityError.PlayServicesNotAvailable ||
                        result.error is IntegrityError.NetworkError ||
                        result.error is IntegrityError.InternalError
                )
            }
        }
    }

    @Test
    fun realIntegrityManager_appIntegrityDoesNotThrow() = runTest(timeout = 60.seconds) {
        val manager = PlayIntegrityManagerImpl(context, DefaultClock())

        val result = manager.verifyAppIntegrity()
        when (result) {
            is Result.Success -> {
                assertNotNull("Status must be present on success", result.data)
                assertTrue("Package name must not be blank", result.data.packageName.isNotBlank())
            }
            is Result.Failure -> {
                assertTrue(
                    "Unexpected integrity error: ${result.error.message}",
                    result.error is IntegrityError.PlayServicesNotAvailable ||
                        result.error is IntegrityError.NetworkError ||
                        result.error is IntegrityError.InternalError
                )
            }
        }
    }
}
