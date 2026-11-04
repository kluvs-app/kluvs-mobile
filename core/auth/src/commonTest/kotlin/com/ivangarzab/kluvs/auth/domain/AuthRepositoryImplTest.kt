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
import dev.mokkery.verify.VerifyMode
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

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
    fun `initialize with no saved session returns null and unauthenticated state`() = runTest {
        // Given
        everySuspend { authService.restoreSession() } returns null

        // When
        val result = repository.initialize()

        // Then
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
        assertNull(repository.currentUser.value)
        assertFalse(repository.isAuthenticated.value)
    }

    @Test
    @OptIn(ExperimentalTime::class)
    fun `initialize with a saved session authenticates the user and removes legacy tokens`() = runTest {
        // Given
        everySuspend { authService.restoreSession() } returns session(UserInfo(aud = "authenticated", id = "user-1"))

        // When
        val result = repository.initialize()

        // Then
        assertEquals("user-1", result.getOrNull()?.id)
        assertEquals("user-1", repository.currentUser.value?.id)
        assertTrue(repository.isAuthenticated.value)
        verify { secureStorage.remove(SecureStorage.KEY_ACCESS_TOKEN) }
        verify { secureStorage.remove(SecureStorage.KEY_REFRESH_TOKEN) }
        verify(VerifyMode.not) { secureStorage.remove(SecureStorage.KEY_USER_ID) }
    }

    @Test
    @OptIn(ExperimentalTime::class)
    fun `initialize fetches the user from the server when the saved session has none`() = runTest {
        // Given
        everySuspend { authService.restoreSession() } returns session(user = null)
        everySuspend { authService.retrieveCurrentUser() } returns UserInfo(aud = "authenticated", id = "user-2")

        // When
        val result = repository.initialize()

        // Then
        assertEquals("user-2", result.getOrNull()?.id)
        assertTrue(repository.isAuthenticated.value)
    }

    @Test
    fun `initialize failure keeps the saved session`() = runTest {
        // Given
        everySuspend { authService.restoreSession() } throws Exception("Unable to resolve host")

        // When
        val result = repository.initialize()

        // Then
        assertTrue(result.isFailure)
        assertEquals(AuthError.NoConnection, result.exceptionOrNull())
        verify(VerifyMode.not) { secureStorage.remove(any()) }
        verify(VerifyMode.not) { secureStorage.clear() }
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

    // ========== Change Password Tests ==========

    @Test
    fun `changePassword succeeds`() = runTest {
        // Given
        val newPassword = "newSecurePassword123"
        everySuspend { authService.updatePassword(newPassword) } returns Unit

        // When
        val result = repository.changePassword(newPassword)

        // Then
        assertTrue(result.isSuccess)
        verifySuspend { authService.updatePassword(newPassword) }
    }

    @Test
    fun `changePassword fails and returns weak password error`() = runTest {
        // Given
        val newPassword = "short"
        everySuspend { authService.updatePassword(newPassword) } throws Exception("Password should be at least 6 characters")

        // When
        val result = repository.changePassword(newPassword)

        // Then
        assertTrue(result.isFailure)
        assertEquals(AuthError.WeakPassword, result.exceptionOrNull())
    }

    // ========== Current User Exists Tests ==========

    @Test
    @OptIn(ExperimentalTime::class)
    fun `currentUserExists returns true when the server returns the user`() = runTest {
        // Given
        everySuspend { authService.retrieveCurrentUser() } returns UserInfo(aud = "authenticated", id = "user-1")

        // When
        val result = repository.currentUserExists()

        // Then
        assertEquals(true, result.getOrNull())
    }

    @Test
    fun `currentUserExists returns false when the server reports the user is gone`() = runTest {
        // Given
        everySuspend { authService.retrieveCurrentUser() } throws Exception("User not found")

        // When
        val result = repository.currentUserExists()

        // Then
        assertEquals(false, result.getOrNull())
    }

    @Test
    fun `currentUserExists fails when the server is unreachable`() = runTest {
        // Given
        everySuspend { authService.retrieveCurrentUser() } throws Exception("Unable to resolve host")

        // When
        val result = repository.currentUserExists()

        // Then
        assertTrue(result.isFailure)
        assertEquals(AuthError.NoConnection, result.exceptionOrNull())
    }

    @OptIn(ExperimentalTime::class)
    private fun session(user: UserInfo?) = UserSession(
        accessToken = "access-token",
        refreshToken = "refresh-token",
        expiresIn = 3600,
        tokenType = "bearer",
        user = user,
    )
}
