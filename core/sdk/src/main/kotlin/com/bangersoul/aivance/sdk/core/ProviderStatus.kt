package com.bangersoul.aivance.sdk.core

/**
 * Lifecycle states for an AI provider.
 */
enum class ProviderStatus {
    Uninitialized,
    Initializing,
    Ready,
    Active,
    Degraded,
    Error,
    Disposed
}
