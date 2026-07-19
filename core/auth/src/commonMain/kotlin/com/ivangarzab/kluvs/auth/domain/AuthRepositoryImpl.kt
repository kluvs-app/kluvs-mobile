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
 * - [SecureStorage] for persistent token storage
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
            // Check if we have stored tokens
            val accessToken = secureStorage.get(SecureStorage.KEY_ACCESS_TOKEN)
            val refreshToken = secureStorage.get(SecureStorage.KEY_REFRESH_TOKEN)

            if (accessToken != null && refreshToken != null) {
                Bark.v("Stored session found, attempting to restore")

                // Restore session
                authService.setSession(accessToken, refreshToken)

                // Get current session to verify it's valid
                val session = authService.getCurrentSession()

                if (session != null) {
                    val user = session.user?.toDomain()
                    if (user != null) {
                        updateAuthState(user)
                        Bark.i("Session restored and user authenticated")
                        return Result.success(user)
                    }
                }

                // Session invalid, clear storage
                Bark.w("Stored session validation failed. Clearing stored credentials.")
                clearStoredSession()
            } else {
                Bark.v("No stored session found")
            }

            Result.success(null)
        } catch (e: Exception) {
            Bark.e("Auth initialization failed. User will be directed to login.", e)
            clearStoredSession()
            Result.failure(e.toAuthError())
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<User> {
        Bark.v("Email sign up initiated")

        return try {
            val session = authService.signUpWithEmail(email, password)
            val user = session.user?.toDomain()
                ?: throw IllegalStateException("Sign up succeeded but user info is missing")

            // Store session tokens
            storeSession(session.accessToken, session.refreshToken)

            // Update state
            updateAuthState(user)

            Bark.i("User successfully signed up and authenticated")
            Result.success(user)
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

            // Store session tokens
            storeSession(session.accessToken, session.refreshToken)

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

            // Store session tokens
            storeSession(session.accessToken, session.refreshToken)

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

            // Store session tokens
            storeSession(session.accessToken, session.refreshToken)

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

    override suspend fun refreshSession(): Result<User> {
        Bark.v("Session refresh initiated")

        return try {
            val session = authService.refreshSession()
            val user = session.user?.toDomain()
                ?: throw IllegalStateException("Session refresh succeeded but user info is missing")

            // Store new tokens
            storeSession(session.accessToken, session.refreshToken)

            // Update state
            updateAuthState(user)

            Bark.d("Session refreshed")
            Result.success(user)
        } catch (e: Exception) {
            Bark.e("Session refresh failed. User will be directed to login.", e)
            // If refresh fails, user needs to sign in again
            clearStoredSession()
            updateAuthState(null)
            Result.failure(e.toAuthError())
        }
    }

    /**
     * Stores session tokens in secure storage.
     */
    private fun storeSession(accessToken: String, refreshToken: String) {
        secureStorage.save(SecureStorage.KEY_ACCESS_TOKEN, accessToken)
        secureStorage.save(SecureStorage.KEY_REFRESH_TOKEN, refreshToken)
        Bark.v("Session tokens stored")
    }

    /**
     * Clears stored session tokens.
     */
    private fun clearStoredSession() {
        secureStorage.remove(SecureStorage.KEY_ACCESS_TOKEN)
        secureStorage.remove(SecureStorage.KEY_REFRESH_TOKEN)
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