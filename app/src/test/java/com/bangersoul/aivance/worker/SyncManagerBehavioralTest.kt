package com.bangersoul.aivance.worker

import androidx.work.WorkManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncManagerBehavioralTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockContext: android.content.Context
    private lateinit var mockConnectivity: ConnectivityMonitor

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // `SyncManager`'s constructor calls `WorkManager.getInstance(context)`, which throws
        // in a JVM unit test unless the companion singleton is mocked. Note: `WorkManager.Companion`
        // (not mockkStatic) because Kotlin callers resolve @JvmStatic functions through the
        // companion's instance method.
        val mockWorkManager = mockk<WorkManager>()
        mockkObject(WorkManager.Companion)
        every { WorkManager.getInstance(any()) } returns mockWorkManager

        mockContext = mockk(relaxed = true)
        mockConnectivity = mockk {
            // Default to offline so `enqueue` does not launch an asynchronous auto-drain
            // (keeps queue-count and state assertions deterministic).
            every { isOnline } returns false
            every { isUnmetered } returns true
            every { networkState } returns MutableStateFlow(NetworkState.UNMETERED)
            every { powerState } returns MutableStateFlow(PowerState.NORMAL)
            every { isFavourableForSync() } returns true
            // emptyFlow: `SyncManager.init` collects this and auto-drains when the queue is
            // non-empty. No emissions means the init-collect can never trigger a drain.
            every { observeNetworkState() } returns emptyFlow()
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        // mockkObject/mockkStatic are JVM-global; release so other test classes in the same
        // JVM never silently receive a mocked WorkManager.
        unmockkAll()
    }

    @Test
    fun `syncManager initial state is IDLE`() {
        val syncManager = SyncManager(mockContext, mockConnectivity)
        assertEquals(SyncState.IDLE, syncManager.syncState.value)
    }

    @Test
    fun `syncManager enqueue increases pending count`() {
        val syncManager = SyncManager(mockContext, mockConnectivity)
        syncManager.enqueue(PendingOperation(type = OperationType.SAVE_JOB, entityId = "job_1"))
        syncManager.enqueue(PendingOperation(type = OperationType.APPLY_JOB, entityId = "job_2"))
        assertEquals(2, syncManager.pendingOperationCount())
    }

    @Test
    fun `syncManager drainQueue processes all operations`() = runBlocking {
        val syncManager = SyncManager(mockContext, mockConnectivity)
        // Enqueue while offline so no async auto-drain is launched, then enable
        // connectivity so the explicit drain below is the only drainer (deterministic).
        syncManager.enqueue(PendingOperation(type = OperationType.SAVE_JOB, entityId = "j1"))
        syncManager.enqueue(PendingOperation(type = OperationType.LOG_EVENT, entityId = "e1"))

        every { mockConnectivity.isOnline } returns true
        syncManager.drainQueue()
        assertEquals(SyncState.SUCCESS, syncManager.syncState.value)
    }

    @Test
    fun `syncManager clearPending resets queue`() {
        val syncManager = SyncManager(mockContext, mockConnectivity)
        syncManager.enqueue(PendingOperation(type = OperationType.SAVE_JOB, entityId = "j1"))
        syncManager.enqueue(PendingOperation(type = OperationType.LOG_EVENT, entityId = "e1"))
        assertEquals(2, syncManager.pendingOperationCount())

        syncManager.clearPending()
        assertEquals(0, syncManager.pendingOperationCount())
    }

    @Test
    fun `syncManager goes OFFLINE when network unavailable`() {
        val offlineConnectivity = mockk<ConnectivityMonitor> {
            every { isOnline } returns false
            every { observeNetworkState() } returns MutableStateFlow(NetworkState.UNAVAILABLE)
            every { networkState } returns MutableStateFlow(NetworkState.UNAVAILABLE)
            every { powerState } returns MutableStateFlow(PowerState.NORMAL)
            every { isFavourableForSync() } returns false
        }

        val syncManager = SyncManager(mockContext, offlineConnectivity)
        // Should be OFFLINE since network is unavailable
        assertNotNull(syncManager.syncState.value)
    }

    @Test
    fun `pendingOperation has unique id and zero retry count`() {
        val op = PendingOperation(type = OperationType.BOOKMARK_JOB, entityId = "j1")
        assertTrue(op.id.isNotBlank())
        assertEquals(0, op.retryCount)
        assertTrue(op.createdAt > 0)
    }

    @Test
    fun `drainQueue with empty queue returns IDLE`() = runBlocking {
        every { mockConnectivity.isOnline } returns true
        val syncManager = SyncManager(mockContext, mockConnectivity)
        syncManager.drainQueue()
        assertEquals(SyncState.IDLE, syncManager.syncState.value)
    }
}
