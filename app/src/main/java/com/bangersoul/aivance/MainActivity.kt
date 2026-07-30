package com.bangersoul.aivance

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bangersoul.aivance.core.data.analytics.PerformanceCollector
import com.bangersoul.aivance.navigation.AivanceNavGraph
import com.bangersoul.aivance.navigation.DeepLinkHandler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single-activity entry point for the Aivance application.
 *
 * Responsibilities:
 * - Apply edge-to-edge rendering
 * - Trigger performance startup timing
 * - Delegate to [AivanceNavGraph] for all UI
 * - Handle deep link intents (aivance://...)
 * - Process death / state restoration via Navigation 3
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var performanceCollector: PerformanceCollector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AivanceNavGraph()
        }

        // Record startup timing after first frame is rendered
        window.decorView.postOnAnimation {
            performanceCollector.recordStartupComplete()
        }

        // Handle deep links from cold start
        handleDeepLinkIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent) {
        DeepLinkHandler.handleIntent(intent)
    }
}
