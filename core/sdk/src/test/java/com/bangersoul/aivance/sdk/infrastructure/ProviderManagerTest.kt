package com.bangersoul.aivance.sdk.infrastructure

import com.bangersoul.aivance.sdk.core.BaseProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.core.ProviderMetadata
import com.bangersoul.aivance.sdk.core.ProviderStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProviderManagerTest {

    private lateinit var registry: ProviderRegistry
    private lateinit var manager: ProviderManager

    @Before
    fun setUp() {
        registry = ProviderRegistry(emptySet(), emptySet())
        manager = ProviderManager(registry)
    }

    @Test
    fun `initializeAll should move providers to Ready status`() = runTest {
        val provider1 = TestProvider("id1")
        val provider2 = TestProvider("id2")
        registry.register(provider1)
        registry.register(provider2)

        manager.initializeAll()

        assertEquals(ProviderStatus.Ready, provider1.status)
        assertEquals(ProviderStatus.Ready, provider2.status)
        assertTrue(provider1.initialized)
        assertTrue(provider2.initialized)
    }

    @Test
    fun `startProvider should move provider to Active status`() = runTest {
        val provider = TestProvider("id1")
        registry.register(provider)

        manager.startProvider("id1")

        assertEquals(ProviderStatus.Active, provider.status)
        assertTrue(provider.initialized)
        assertTrue(provider.started)
    }

    @Test
    fun `stopProvider should move provider back to Ready status`() = runTest {
        val provider = TestProvider("id1")
        registry.register(provider)

        manager.startProvider("id1")
        assertEquals(ProviderStatus.Active, provider.status)

        manager.stopProvider("id1")
        assertEquals(ProviderStatus.Ready, provider.status)
        assertTrue(provider.stopped)
    }

    @Test
    fun `getBestProviderFor should prioritize Active providers`() {
        val provider1 = TestProvider("id1", setOf(ProviderCapability.TextAnalysis))
        val provider2 = TestProvider("id2", setOf(ProviderCapability.TextAnalysis))
        registry.register(provider1)
        registry.register(provider2)

        // Initial state: both Uninitialized. Order not guaranteed.
        val initialBest = manager.getBestProviderFor(ProviderCapability.TextAnalysis)
        assertTrue(initialBest == provider1 || initialBest == provider2)

        // provider2 is Ready, provider1 is Uninitialized
        provider2.updateStatus(ProviderStatus.Ready)
        assertEquals(provider2, manager.getBestProviderFor(ProviderCapability.TextAnalysis))

        // provider1 is Active, provider2 is Ready
        provider1.updateStatus(ProviderStatus.Active)
        assertEquals(provider1, manager.getBestProviderFor(ProviderCapability.TextAnalysis))
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
        var initialized = false
        var started = false
        var stopped = false
        var disposed = false

        override suspend fun onInitialize() {
            initialized = true
        }

        override suspend fun onStart() {
            started = true
        }

        override suspend fun onStop() {
            stopped = true
        }

        override suspend fun onDispose() {
            disposed = true
        }
    }
}
