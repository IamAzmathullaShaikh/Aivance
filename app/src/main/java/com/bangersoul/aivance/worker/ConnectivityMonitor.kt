package com.bangersoul.aivance.worker

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.BatteryManager
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.net.URLConnection
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Network connectivity state.
 */
enum class NetworkState {
    UNAVAILABLE,
    METERED,
    UNMETERED
}

/**
 * Battery and power state for scheduling decisions.
 */
enum class PowerState {
    NORMAL,
    CHARGING,
    BATTERY_SAVER
}

/**
 * Monitors real-time network connectivity and power state.
 *
 * Used by [SyncManager] and workers to make battery-aware scheduling decisions.
 */
@Singleton
class ConnectivityMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager = context.getSystemService<ConnectivityManager>()

    private val _networkState = MutableStateFlow(checkCurrentNetworkState())
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private val _powerState = MutableStateFlow(checkPowerState())
    val powerState: StateFlow<PowerState> = _powerState.asStateFlow()

    /** True when the device has an active internet connection. */
    val isOnline: Boolean get() = _networkState.value != NetworkState.UNAVAILABLE

    /** True when the connection is unmetered (Wi-Fi). */
    val isUnmetered: Boolean get() = _networkState.value == NetworkState.UNMETERED

    /**
     * Returns a cold [Flow] that emits [NetworkState] changes.
     * Properly unregisters the callback when the flow collection is cancelled.
     */
    fun observeNetworkState(): Flow<NetworkState> = callbackFlow {
        val currentState = checkCurrentNetworkState()
        _networkState.value = currentState
        trySend(currentState)

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val state = checkCurrentNetworkState()
                _networkState.value = state
                trySend(state)
            }

            override fun onLost(network: Network) {
                _networkState.value = NetworkState.UNAVAILABLE
                trySend(NetworkState.UNAVAILABLE)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                val state = when {
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) ->
                        NetworkState.UNMETERED
                    else -> NetworkState.METERED
                }
                _networkState.value = state
                trySend(state)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager?.registerNetworkCallback(request, callback)

        awaitClose {
            connectivityManager?.unregisterNetworkCallback(callback)
        }
    }

    /**
     * Returns true when conditions are favourable for background sync:
     * - Device is charging (or has sufficient battery)
     * - Network is available (preferably unmetered)
     */
    fun isFavourableForSync(): Boolean {
        val network = _networkState.value
        val power = _powerState.value
        return network != NetworkState.UNAVAILABLE &&
                (power == PowerState.CHARGING || power == PowerState.NORMAL)
    }

    /**
     * Checks if the device can actually reach the public internet
     * (beyond just having an active network interface).
     */
    suspend fun isInternetReachable(): Boolean {
        return try {
            val url = URL("https://clients3.google.com/generate_204")
            val connection: URLConnection = url.openConnection()
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
            reader.close()
            true
        } catch (_: Exception) {
            false
        }
    }

    // ── Private helpers ─────────────────────────────

    private fun checkCurrentNetworkState(): NetworkState {
        val activeNetwork = connectivityManager?.activeNetwork ?: return NetworkState.UNAVAILABLE
        val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork) ?: return NetworkState.UNAVAILABLE

        return when {
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) ->
                NetworkState.UNMETERED
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ->
                NetworkState.METERED
            else -> NetworkState.UNAVAILABLE
        }
    }

    private fun checkPowerState(): PowerState {
        val batteryManager = context.getSystemService<BatteryManager>()
        return if (batteryManager != null) {
            val isCharging = batteryManager.isCharging
            if (isCharging) PowerState.CHARGING else PowerState.NORMAL
        } else {
            PowerState.NORMAL
        }
    }
}
