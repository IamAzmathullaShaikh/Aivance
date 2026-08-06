package com.bangersoul.aivance.navigation

import androidx.activity.ComponentActivity
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.internal.GeneratedComponentManager
import dagger.hilt.internal.GeneratedComponentManagerHolder

/**
 * Host activity for Compose UI tests that create Hilt ViewModels via
 * `hiltViewModel()`. Mirrors the classic
 * `dagger.hilt.android.testing.HiltTestActivity` that Hilt 2.51 no longer
 * ships: `@HiltAndroidTest` makes Hilt generate the test component and
 * [generatedComponent] delegates to the [HiltTestApplication]'s component.
 * Deliberately does NOT call setContent — the compose test rule populates it.
 */
@HiltAndroidTest
class HiltTestActivity : ComponentActivity(), GeneratedComponentManager<Any> {

    private var componentManager: Any? = null

    override fun generatedComponent(): Any {
        if (componentManager == null) {
            synchronized(this) {
                if (componentManager == null) {
                    componentManager = (application as GeneratedComponentManagerHolder).generatedComponent()
                }
            }
        }
        return componentManager!!
    }
}
