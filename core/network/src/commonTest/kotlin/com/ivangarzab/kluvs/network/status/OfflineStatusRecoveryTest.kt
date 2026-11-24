package com.ivangarzab.kluvs.network.status

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineStatusRecoveryTest {

    private var reachable = false
    private var probeCalls = 0

    private val probe = ConnectivityCheck {
        probeCalls++
        reachable
    }

    private fun TestScope.observedStatus(): OfflineStatus {
        val status = OfflineStatus(probe, backgroundScope, 5.seconds, 30.seconds)
        backgroundScope.launch { status.isOffline.collect { } }
        runCurrent()
        return status
    }

    private fun TestScope.advance(seconds: Int) {
        advanceTimeBy(seconds.seconds)
        runCurrent()
    }

    @Test
    fun recoversOnceTheProbeSucceeds() = runTest {
        val status = observedStatus()
        status.markOffline()
        runCurrent()

        advance(5)
        assertEquals(1, probeCalls)
        assertTrue(status.isOffline.value)

        reachable = true
        advance(10)

        assertEquals(2, probeCalls)
        assertFalse(status.isOffline.value)
    }

    @Test
    fun backsOffThenHoldsAtTheMaximum() = runTest {
        val status = observedStatus()
        status.markOffline()
        runCurrent()

        // Probes at 5s then +10s then +20s then +30s then +30s
        advance(5)
        assertEquals(1, probeCalls)
        advance(10)
        assertEquals(2, probeCalls)
        advance(20)
        assertEquals(3, probeCalls)
        advance(30)
        assertEquals(4, probeCalls)
        advance(29)
        assertEquals(4, probeCalls)
        advance(1)
        assertEquals(5, probeCalls)
    }

    @Test
    fun doesNotProbeWhileOnline() = runTest {
        observedStatus()

        advance(120)

        assertEquals(0, probeCalls)
    }

    @Test
    fun doesNotProbeWithoutAnObserver() = runTest {
        val status = OfflineStatus(probe, backgroundScope, 5.seconds, 30.seconds)
        status.markOffline()
        runCurrent()

        advance(120)

        assertEquals(0, probeCalls)
        assertTrue(status.isOffline.value)
    }

    @Test
    fun stopsProbingOnceBackOnlineByOtherMeans() = runTest {
        val status = observedStatus()
        status.markOffline()
        runCurrent()
        advance(5)
        assertEquals(1, probeCalls)

        // A real request got a response, e.g. a pull-to-refresh
        status.markOnline()
        runCurrent()
        advance(120)

        assertEquals(1, probeCalls)
    }

    @Test
    fun restartsFromTheShortDelayAfterGoingOfflineAgain() = runTest {
        val status = observedStatus()
        status.markOffline()
        runCurrent()
        advance(5)
        advance(10)
        assertEquals(2, probeCalls)

        status.markOnline()
        runCurrent()
        status.markOffline()
        runCurrent()

        advance(5)
        assertEquals(3, probeCalls)
    }
}
