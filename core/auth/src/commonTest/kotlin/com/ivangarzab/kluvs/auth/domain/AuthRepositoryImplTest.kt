package com.ivangarzab.kluvs.auth.domain

import com.ivangarzab.kluvs.auth.persistence.SecureStorage
import com.ivangarzab.kluvs.auth.remote.AuthService
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for AuthRepositoryImpl.
 *
 * Note: We focus on testing the repository logic (error handling, storage management, state updates)
 * rather than Supabase integration details. Full integration tests would require actual Supabase instance.
 */
class AuthRepositoryImplTest {

    private lateinit var authService: AuthService
    private lateinit var secureStorage: SecureStorage
    private lateinit var repository: AuthRepositoryImpl

    @BeforeTest
    fun setup() {
        authService = mock<AuthService>()
        secureStorage = mock<SecureStorage>()

        // Setup default stubs for secureStorage methods that don't need specific behavior
        every { secureStorage.save(any(), any()) } returns Unit
        every { secureStorage.remove(any()) } returns Unit
        every { secureStorage.clear() } returns Unit

        repository = AuthRepositoryImpl(authService, secureStorage)
    }

    // ========== Initialize Tests ==========

    @Test
    fun `initialize with no stored session returns null and unauthenticated state`() = runTest {
        // Given
        every { secureStorage.get(SecureStorage.KEY_ACCESS_TOKEN) } returns null
        every { secureStorage.get(SecureStorage.KEY_REFRESH_TOKEN) } returns null

        // When
        val result = repository.initialize()

        // Then
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
        assertNull(repository.currentUser.value)
        assertFalse(repository.isAuthenticated.value)
    }

    @Test
    fun `initialize with invalid stored session clears storage`() = runTest {
        // Given
        val accessToken = "invalid-access-token"
        val refreshToken = "invalid-refresh-token"

        every { secureStorage.get(SecureStorage.KEY_ACCESS_TOKEN) } returns accessToken
        every { secureStorage.get(SecureStorage.KEY_REFRESH_TOKEN) } returns refreshToken
        everySuspend { authService.setSession(accessToken, refreshToken) } returns Unit
        everySuspend { authService.getCurrentSession() } returns null  // Invalid session

        // When
        val result = repository.initialize()

        // Then
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
        verify { secureStorage.remove(SecureStorage.KEY_ACCESS_TOKEN) }
        verify { secureStorage.remove(SecureStorage.KEY_REFRESH_TOKEN) }
        verify { secureStorage.remove(SecureStorage.KEY_USER_ID) }
    }

    @Test
    fun `initialize handles exception and clears storage`() = runTest {
        // Given
        every { secureStorage.get(SecureStorage.KEY_ACCESS_TOKEN) } returns "token"
        every { secureStorage.get(SecureStorage.KEY_REFRESH_TOKEN) } returns "refresh"
        everySuspend { authService.setSession(any(), any()) } throws Exception("Network error")

        // When
        val result = repository.initialize()

        // Then
        assertTrue(result.isFailure)
        verify { secureStorage.remove(SecureStorage.KEY_ACCESS_TOKEN) }
        verify { secureStorage.remove(SecureStorage.KEY_REFRESH_TOKEN) }
        verify { secureStorage.remove(SecureStorage.KEY_USER_ID) }
    }

    // ========== Sign Up Tests ==========

    @Test
    fun `signUpWithEmail fails and returns weak password error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "weak"
        everySuspend { authService.signUpWithEmail(email, password) } throws Exception("Password should be at least 6 characters")

        // When
        val result = repository.signUpWithEmail(email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals(AuthError.WeakPassword, result.exceptionOrNull())
        assertNull(repository.currentUser.value)
        assertFalse(repository.isAuthenticated.value)
    }

    @Test
    fun `signUpWithEmail fails when user already exists`() = runTest {
        // Given
        val email = "existing@example.com"
        val password = "password123"
        everySuspend { authService.signUpWithEmail(email, password) } throws Exception("User already registered")

        // When
        val result = repository.signUpWithEmail(email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals(AuthError.UserAlreadyExists, result.exceptionOrNull())
    }

    // ========== Sign In Tests ==========

    @Test
    fun `signInWithEmail fails with invalid credentials`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrong-password"
        everySuspend { authService.signInWithEmail(email, password) } throws Exception("Invalid login credentials")

        // When
        val result = repository.signInWithEmail(email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals(AuthError.InvalidCredentials, result.exceptionOrNull())
        assertNull(repository.currentUser.value)
        assertFalse(repository.isAuthenticated.value)
    }

    // ========== OAuth Tests ==========

    @Test
    fun `signInWithDiscord returns OAuth URL`() = runTest {
        // Given
        val expectedUrl = "https://discord.com/oauth/authorize?..."
        everySuspend { authService.getOAuthUrl("discord") } returns expectedUrl

        // When
        val result = repository.signInWithDiscord()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedUrl, result.getOrNull())
        verifySuspend { authService.getOAuthUrl("discord") }
    }

    @Test
    fun `signInWithGoogle returns OAuth URL`() = runTest {
        // Given
        val expectedUrl = "https://accounts.google.com/o/oauth2/v2/auth?..."
        everySuspend { authService.getOAuthUrl("google") } returns expectedUrl

        // When
        val result = repository.signInWithGoogle()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedUrl, result.getOrNull())
        verifySuspend { authService.getOAuthUrl("google") }
    }

    // ========== Sign Out Tests ==========

    @Test
    fun `signOut calls service and clears storage`() = runTest {
        // Given
        everySuspend { authService.signOut() } returns Unit

        // When
        val result = repository.signOut()

        // Then
        assertTrue(result.isSuccess)
        assertNull(repository.currentUser.value)
        assertFalse(repository.isAuthenticated.value)
        verify { secureStorage.remove(SecureStorage.KEY_ACCESS_TOKEN) }
        verify { secureStorage.remove(SecureStorage.KEY_REFRESH_TOKEN) }
        verify { secureStorage.remove(SecureStorage.KEY_USER_ID) }
        verifySuspend { authService.signOut() }
    }

    @Test
    fun `signOut clears local state even when service call fails`() = runTest {
        // Given
        everySuspend { authService.signOut() } throws Exception("Network error")

        // When
        val result = repository.signOut()

        // Then
        assertTrue(result.isFailure)
        // But local state is still cleared
        assertNull(repository.currentUser.value)
        assertFalse(repository.isAuthenticated.value)
        verify { secureStorage.remove(SecureStorage.KEY_ACCESS_TOKEN) }
        verify { secureStorage.remove(SecureStorage.KEY_REFRESH_TOKEN) }
        verify { secureStorage.remove(SecureStorage.KEY_USER_ID) }
    }

    // ========== Reset Password Tests ==========

    @Test
    fun `resetPasswordForEmail succeeds`() = runTest {
        // Given
        val email = "test@example.com"
        everySuspend { authService.resetPasswordForEmail(email) } returns Unit

        // When
        val result = repository.resetPasswordForEmail(email)

        // Then
        assertTrue(result.isSuccess)
        verifySuspend { authService.resetPasswordForEmail(email) }
    }

    @Test
    fun `resetPasswordForEmail fails and returns user not found error`() = runTest {
        // Given
        val email = "unknown@example.com"
        everySuspend { authService.resetPasswordForEmail(email) } throws Exception("User not found")

        // When
        val result = repository.resetPasswordForEmail(email)

        // Then
        assertTrue(result.isFailure)
        assertEquals(AuthError.UserNotFound, result.exceptionOrNull())
    }

    // ========== Refresh Session Tests ==========

    @Test
    fun `refreshSession fails and clears storage and state`() = runTest {
        // Given
        everySuspend { authService.refreshSession() } throws Exception("Refresh token expired")

        // When
        val result = repository.refreshSession()

        // Then
        assertTrue(result.isFailure)
        assertNull(repository.currentUser.value)
        assertFalse(repository.isAuthenticated.value)
        verify { secureStorage.remove(SecureStorage.KEY_ACCESS_TOKEN) }
        verify { secureStorage.remove(SecureStorage.KEY_REFRESH_TOKEN) }
        verify { secureStorage.remove(SecureStorage.KEY_USER_ID) }
    }
}