package com.ivangarzab.kluvs.auth.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.auth.remote.AuthService
import com.ivangarzab.kluvs.auth.mappers.toAuthError
import com.ivangarzab.kluvs.auth.mappers.toDomain
import com.ivangarzab.kluvs.auth.persistence.SecureStorage
import com.ivangarzab.kluvs.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementation of [AuthRepository].
 *
 * Coordinates between:
 * - [AuthService] for Supabase authentication
 * - [SecureStorage] for the signed-in user ID (Supabase persists the session itself)
 * - StateFlow for reactive auth state
 */
class AuthRepositoryImpl(
    private val authService: AuthService,
    private val secureStorage: SecureStorage
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    override val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    override suspend fun initialize(): Result<User?> {
        Bark.v("Auth initialization started")

        return try {
            val session = authService.restoreSession()
            if (session == null) {
                Bark.v("No session to restore")
                updateAuthState(null)
                return Result.success(null)
            }
            val user = (session.user ?: authService.retrieveCurrentUser()).toDomain()
            // Tokens written here by older app versions go stale as Supabase rotates them, and
            // were what signed users out on launch. Supabase's own storage is the source of truth.
            removeLegacyTokens()
            updateAuthState(user)
            Bark.i("Session restored and user authenticated")
            Result.success(user)
        } catch (e: Exception) {
            // Never clear the saved session here: a transient failure (e.g. no connection) must
            // not sign the user out. Supabase only drops the session if the server revokes it.
            Bark.e("Auth initialization failed. Saved session kept for the next attempt.", e)
            Result.failure(e.toAuthError())
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<User> {
        Bark.v("Email sign up initiated")

        return try {
            val session = authService.signUpWithEmail(email, password)
            val user = session.user?.toDomain()
                ?: throw IllegalStateException("Sign up succeeded but user info is missing")

            // Update state
            updateAuthState(user)

            Bark.i("User successfully signed up and authenticated")
            Result.success(user)
        } catch (e: EmailConfirmationRequiredException) {
            Bark.i("Email sign up succeeded, pending email confirmation")
            Result.failure(e.toAuthError())
        } catch (e: Exception) {
            Bark.e("Email sign up failed. Check email format and try again.", e)
            Result.failure(e.toAuthError())
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        Bark.v("Email sign in initiated")

        return try {
            val session = authService.signInWithEmail(email, password)
            val user = session.user?.toDomain()
                ?: throw IllegalStateException("Sign in succeeded but user info is missing")

            // Update state
            updateAuthState(user)

            Bark.i("User successfully signed in")
            Result.success(user)
        } catch (e: Exception) {
            Bark.e("Email sign in failed. Verify credentials and retry.", e)
            Result.failure(e.toAuthError())
        }
    }

    override suspend fun signInWithDiscord(): Result<String> {
        Bark.v("Discord OAuth URL generation started")

        return try {
            val url = authService.getOAuthUrl("discord")
            Bark.d("Discord OAuth URL generated")
            Result.success(url)
        } catch (e: Exception) {
            Bark.e("Discord OAuth URL generation failed. Sign in via Discord unavailable.", e)
            Result.failure(e.toAuthError())
        }
    }

    override suspend fun signInWithGoogle(): Result<String> {
        Bark.v("Google OAuth URL generation started")

        return try {
            val url = authService.getOAuthUrl("google")
            Bark.d("Google OAuth URL generated")
            Result.success(url)
        } catch (e: Exception) {
            Bark.e("Google OAuth URL generation failed. Sign in via Google unavailable.", e)
            Result.failure(e.toAuthError())
        }
    }

    override suspend fun handleOAuthCallback(callbackUrl: String): Result<User> {
        Bark.v("OAuth callback processing started")

        return try {
            val session = authService.handleOAuthCallback(callbackUrl)
            val user = session.user?.toDomain()
                ?: throw IllegalStateException("OAuth succeeded but user info is missing")

            // Update state
            updateAuthState(user)

            Bark.i("User successfully authenticated via OAuth")
            Result.success(user)
        } catch (e: Exception) {
            Bark.e("OAuth callback processing failed. User will need to retry OAuth sign in.", e)
            Result.failure(e.toAuthError())
        }
    }

    override suspend fun signInWithAppleNative(idToken: String): Result<User> {
        Bark.v("Apple native ID token sign in initiated")

        return try {
            val session = authService.signInWithAppleIdToken(idToken)
            val user = session.user?.toDomain()
                ?: throw IllegalStateException("Apple Sign In succeeded but user info is missing")

            // Update state
            updateAuthState(user)

            Bark.i("User successfully authenticated via Apple ID")
            Result.success(user)
        } catch (e: Exception) {
            Bark.e("Apple ID sign in failed. Please retry.", e)
            Result.failure(e.toAuthError())
        }
    }

    override suspend fun resetPasswordForEmail(email: String): Result<Unit> {
        Bark.v("Password reset email requested")

        return try {
            authService.resetPasswordForEmail(email)
            Bark.i("Password reset email sent")
            Result.success(Unit)
        } catch (e: Exception) {
            Bark.e("Password reset email failed to send. User may need to retry.", e)
            Result.failure(e.toAuthError())
        }
    }

    override suspend fun changePassword(newPassword: String): Result<Unit> {
        Bark.v("Password change requested")

        return try {
            authService.updatePassword(newPassword)
            Bark.i("Password changed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Bark.e("Password change failed. User may need to retry.", e)
            Result.failure(e.toAuthError())
        }
    }

    override suspend fun signOut(): Result<Unit> {
        Bark.v("Sign out initiated")

        return try {
            // Sign out from Supabase
            authService.signOut()

            // Clear stored session
            clearStoredSession()

            // Update state
            updateAuthState(null)

            Bark.i("User signed out")
            Result.success(Unit)
        } catch (e: Exception) {
            Bark.e("Sign out failed but local session cleared. User may still be authenticated on server.", e)
            // Still clear local state even if server sign out fails
            clearStoredSession()
            updateAuthState(null)
            Result.failure(e.toAuthError())
        }
    }

    override suspend fun currentUserExists(): Result<Boolean> {
        Bark.v("Checking whether current user still exists")

        return try {
            authService.retrieveCurrentUser()
            Result.success(true)
        } catch (e: Exception) {
            when (val error = e.toAuthError()) {
                AuthError.UserNotFound -> {
                    Bark.i("Current user no longer exists on server")
                    Result.success(false)
                }
                else -> {
                    Bark.w("Could not verify whether current user exists.", e)
                    Result.failure(error)
                }
            }
        }
    }

    private fun removeLegacyTokens() {
        secureStorage.remove(SecureStorage.KEY_ACCESS_TOKEN)
        secureStorage.remove(SecureStorage.KEY_REFRESH_TOKEN)
    }

    /**
     * Clears stored session data.
     */
    private fun clearStoredSession() {
        removeLegacyTokens()
        secureStorage.remove(SecureStorage.KEY_USER_ID)
        Bark.v("Stored credentials cleared")
    }

    /**
     * Updates auth state flows.
     */
    private fun updateAuthState(user: User?) {
        _currentUser.value = user
        _isAuthenticated.value = user != null

        // Optionally store user ID for quick access
        if (user != null) {
            secureStorage.save(SecureStorage.KEY_USER_ID, user.id)
        }
    }
}