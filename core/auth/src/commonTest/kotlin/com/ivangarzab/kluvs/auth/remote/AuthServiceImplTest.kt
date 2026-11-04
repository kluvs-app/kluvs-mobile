package com.ivangarzab.kluvs.auth.remote

import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

/**
 * Tests for [AuthServiceImpl.restoreSession], driving Supabase's session status by hand.
 */
@OptIn(ExperimentalTime::class)
class AuthServiceImplTest {

    private lateinit var auth: Auth
    private lateinit var sessionManager: SessionManager
    private lateinit var sessionStatus: MutableStateFlow<SessionStatus>
    private lateinit var service: AuthServiceImpl

    private val savedSession = UserSession(
        accessToken = "access-token",
        refreshToken = "refresh-token",
        expiresIn = 3600,
        tokenType = "bearer",
    )

    @BeforeTest
    fun setup() {
        auth = mock<Auth>()
        sessionManager = mock<SessionManager>()
        sessionStatus = MutableStateFlow(SessionStatus.Initializing)

        every { auth.sessionStatus } returns sessionStatus
        every { auth.sessionManager } returns sessionManager
        // loadFromStorage()'s default argument reads the config
        every { auth.config } returns AuthConfig()
        everySuspend { sessionManager.loadSession() } returns savedSession

        service = AuthServiceImpl { auth }
    }

    @Test
    fun `restoreSession returns the session Supabase restored`() = runTest {
        // Given
        sessionStatus.value = SessionStatus.Authenticated(savedSession)

        // When
        val result = service.restoreSession()

        // Then
        assertEquals(savedSession, result)
        verifySuspend(VerifyMode.not) { auth.loadFromStorage(any()) }
    }

    @Test
    fun `restoreSession returns null when Supabase has no session`() = runTest {
        // Given
        sessionStatus.value = SessionStatus.NotAuthenticated()

        // When
        val result = service.restoreSession()

        // Then
        assertNull(result)
    }

    @Test
    fun `restoreSession returns the saved session when the refresh failed`() = runTest {
        // Given
        sessionStatus.value = SessionStatus.RefreshFailure(RefreshFailureCause.NetworkError(Exception("Offline")))

        // When
        val result = service.restoreSession()

        // Then
        assertEquals(savedSession, result)
    }

    @Test
    fun `restoreSession reloads a paused session from storage`() = runTest {
        // Given
        every { auth.isAutoRefreshRunning } returns false
        everySuspend { auth.loadFromStorage(any()) } calls {
            sessionStatus.value = SessionStatus.Authenticated(savedSession)
            true
        }

        // When
        val result = service.restoreSession()

        // Then
        assertEquals(savedSession, result)
        verifySuspend(VerifyMode.exactly(1)) { auth.loadFromStorage(any()) }
    }

    @Test
    fun `restoreSession returns null when a paused session has nothing saved`() = runTest {
        // Given
        every { auth.isAutoRefreshRunning } returns false
        everySuspend { auth.loadFromStorage(any()) } returns false

        // When
        val result = service.restoreSession()

        // Then
        assertNull(result)
    }

    @Test
    fun `restoreSession waits only briefly for a pending refresh when the saved token is still valid`() = runTest {
        // Given
        every { auth.isAutoRefreshRunning } returns true

        // When
        val result = service.restoreSession()

        // Then
        assertEquals(savedSession, result)
        assertEquals(3_000L, testScheduler.currentTime)
        verifySuspend(VerifyMode.not) { auth.loadFromStorage(any()) }
    }

    @Test
    fun `restoreSession waits the full timeout for a pending refresh when the saved token expired`() = runTest {
        // Given
        val expiredSession = savedSession.copy(expiresAt = Clock.System.now() - 1.minutes)
        every { auth.isAutoRefreshRunning } returns true
        everySuspend { sessionManager.loadSession() } returns expiredSession

        // When
        val result = service.restoreSession()

        // Then
        assertEquals(expiredSession, result)
        assertEquals(11_000L, testScheduler.currentTime)
    }

    @Test
    fun `restoreSession uses a reloaded session that is authenticated straight away`() = runTest {
        // Given
        every { auth.isAutoRefreshRunning } returns false
        everySuspend { auth.loadFromStorage(any()) } calls {
            sessionStatus.value = SessionStatus.Authenticated(savedSession)
            true
        }

        // When
        service.restoreSession()

        // Then
        assertEquals(1_000L, testScheduler.currentTime)
    }

    @Test
    fun `concurrent restores reload a paused session only once`() = runTest {
        // Given
        every { auth.isAutoRefreshRunning } returns false
        everySuspend { auth.loadFromStorage(any()) } calls {
            sessionStatus.value = SessionStatus.Authenticated(savedSession)
            true
        }

        // When
        val results = List(2) { async { service.restoreSession() } }.awaitAll()

        // Then
        assertEquals(listOf(savedSession, savedSession), results)
        verifySuspend(VerifyMode.exactly(1)) { auth.loadFromStorage(any()) }
    }
}
