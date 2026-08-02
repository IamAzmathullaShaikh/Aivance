package com.bangersoul.aivance

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.bangersoul.aivance.core.data.analytics.PerformanceCollector
import com.bangersoul.aivance.core.datastore.UserPreferencesRepository
import com.bangersoul.aivance.navigation.AivanceNavGraph
import com.bangersoul.aivance.navigation.DeepLinkHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/**
 * Single-activity entry point for the Aivance application.
 *
 * Responsibilities:
 * - Apply edge-to-edge rendering
 * - Apply the user's chosen app language from DataStore
 * - Trigger performance startup timing
 * - Delegate to [AivanceNavGraph] for all UI
 * - Handle deep link intents (aivance://...)
 * - Process death / state restoration via Navigation 3
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var performanceCollector: PerformanceCollector

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun attachBaseContext(newBase: Context) {
        // Wrap the base context with the persisted language so configuration
        // changes (dates, direction, pickers) follow the user's choice on every
        // API level — works before the async preference read below settles.
        super.attachBaseContext(wrapWithLanguage(newBase, cachedLanguage))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AivanceNavGraph()
        }

        // Apply the persisted language preference to the app configuration.
        // On first creation the cache defaults to "en" while the async read
        // settles; if a different language is stored we wrap the context and
        // recreate so attachBaseContext picks it up on the next pass.
        lifecycleScope.launch {
            val language = runCatching {
                userPreferencesRepository.userPreferences.first().language
            }.getOrDefault("en")
            if (language != cachedLanguage) {
                cachedLanguage = language
                recreate()
            }
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

    companion object {
        /** Locale tag applied in [wrapWithLanguage] — cached to avoid infinite recreate loops. */
        private var cachedLanguage: String = "en"

        /**
         * Returns a context whose resources are configured for [languageCode].
         * Uses [Context.createConfigurationContext] (works on every API level,
         * unlike the deprecated [android.content.res.Resources.updateConfiguration]
         * which is ignored on API 33+). Falls back to the plain context when the
         * code is unparsable or "en".
         */
        private fun wrapWithLanguage(context: Context, languageCode: String): Context {
            if (languageCode == "en" || languageCode.isBlank()) return context
            return try {
                val locale = Locale.forLanguageTag(languageCode)
                Locale.setDefault(locale)
                val config = Configuration(context.resources.configuration)
                config.setLocale(locale)
                config.setLayoutDirection(locale)
                context.createConfigurationContext(config)
            } catch (_: Exception) {
                context
            }
        }
    }
}
