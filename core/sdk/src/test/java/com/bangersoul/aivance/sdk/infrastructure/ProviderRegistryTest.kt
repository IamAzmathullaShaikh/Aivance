package com.bangersoul.aivance.sdk.infrastructure

import com.bangersoul.aivance.sdk.core.BaseProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.core.ProviderMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProviderRegistryTest {

    private lateinit var registry: ProviderRegistry

    @Before
    fun setUp() {
        registry = ProviderRegistry(emptySet(), emptySet())
    }

    @Test
    fun `register should add provider to registry`() {
        val provider = TestProvider("test-id")
        registry.register(provider)

        assertEquals(provider, registry.getProvider("test-id"))
    }

    @Test
    fun `unregister should remove provider from registry`() {
        val provider = TestProvider("test-id")
        registry.register(provider)
        registry.unregister("test-id")

        assertNull(registry.getProvider("test-id"))
    }

    @Test
    fun `getProvidersByCapability should return filtered providers`() {
        val provider1 = TestProvider("id1", setOf(ProviderCapability.TextAnalysis))
        val provider2 = TestProvider("id2", setOf(ProviderCapability.ImageProcessing))
        val provider3 = TestProvider("id3", setOf(ProviderCapability.TextAnalysis, ProviderCapability.JobSearch))

        registry.register(provider1)
        registry.register(provider2)
        registry.register(provider3)

        val textProviders = registry.getProvidersByCapability(ProviderCapability.TextAnalysis)
        assertEquals(2, textProviders.size)
        assertTrue(textProviders.contains(provider1))
        assertTrue(textProviders.contains(provider3))
    }

    @Test
    fun `getAllProviders should return all registered providers`() {
        val provider1 = TestProvider("id1")
        val provider2 = TestProvider("id2")

        registry.register(provider1)
        registry.register(provider2)

        val allProviders = registry.getAllProviders()
        assertEquals(2, allProviders.size)
        assertTrue(allProviders.contains(provider1))
        assertTrue(allProviders.contains(provider2))
    }

    private class TestProvider(
        id: String,
        capabilities: Set<ProviderCapability> = emptySet()
    ) : BaseProvider(
        metadata = ProviderMetadata(
            id = id,
            name = "Test Provider $id",
            version = "1.0.0",
            description = "Test Description",
            author = "Tester"
        ),
        capabilities = capabilities
    ) {
        override suspend fun onInitialize() {}
        override suspend fun onStart() {}
        override suspend fun onStop() {}
        override suspend fun onDispose() {}
    }
}
