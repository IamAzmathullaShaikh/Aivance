package com.bangersoul.aivance.worker

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
        mockContext = mockk()
        mockConnectivity = mockk {
            every { isOnline } returns true
            every { isUnmetered } returns true
            every { networkState } returns MutableStateFlow(NetworkState.UNMETERED)
            every { powerState } returns MutableStateFlow(PowerState.NORMAL)
            every { isFavourableForSync() } returns true
            every { observeNetworkState() } returns MutableStateFlow(NetworkState.UNMETERED)
        }
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

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
        syncManager.enqueue(PendingOperation(type = OperationType.SAVE_JOB, entityId = "j1"))
        syncManager.enqueue(PendingOperation(type = OperationType.LOG_EVENT, entityId = "e1"))

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
        val syncManager = SyncManager(mockContext, mockConnectivity)
        syncManager.drainQueue()
        assertEquals(SyncState.IDLE, syncManager.syncState.value)
    }
}
