package com.bangersoul.aivance.sdk.core

/**
 * Lifecycle and health states for a provider.
 */
enum class ProviderStatus {
    /** Initial state before any setup. */
    Uninitialized,

    /** Setup is in progress. */
    Initializing,

    /** Configured and passed basic health checks, but not currently in use. */
    Ready,

    /** Fully operational and actively handling requests. */
    Active,

    /** Operational but with some issues. */
    Healthy,

    /** Operating with limited functionality. */
    Degraded,

    /** The current configuration is invalid or incomplete. */
    InvalidConfiguration,

    /** Credentials failed (e.g., wrong API Key). */
    AuthenticationFailed,

    /** Reached usage limits. */
    RateLimited,

    /** Device has no internet connection. */
    Offline,

    /** The provider service itself is down. */
    Unavailable,

    /** An unrecoverable internal error occurred. */
    Error,

    /** Cleaned up and resources released. */
    Disposed
}
