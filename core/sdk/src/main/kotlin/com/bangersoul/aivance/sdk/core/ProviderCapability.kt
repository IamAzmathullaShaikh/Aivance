package com.bangersoul.aivance.sdk.core

/**
 * Represents the various capabilities an AI provider can support.
 */
sealed class ProviderCapability {
    data object TextAnalysis : ProviderCapability()
    data object ImageProcessing : ProviderCapability()
    data object JobSearch : ProviderCapability()
    
    sealed class AI : ProviderCapability() {
        data object Chat : AI()
        data object TextGeneration : AI()
        data object Vision : AI()
        data object Streaming : AI()
        data object FunctionCalling : AI()
    }
    
    /**
     * For providers with unique or specialized capabilities not covered by standard types.
     */
    data class Custom(val name: String) : ProviderCapability()
}
