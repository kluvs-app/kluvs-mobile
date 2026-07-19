package com.ivangarzab.kluvs.auth.domain

import com.ivangarzab.kluvs.model.User
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository for managing user authentication state.
 *
 * This is the single source of truth for authentication in the app.
 * It coordinates between AuthService (Supabase Auth) and SecureStorage
 * (persistent token storage).
 */
interface AuthRepository {

    /**
     * Current authenticated user.
     * Emits null when user is not authenticated.
     */
    val currentUser: StateFlow<User?>

    /**
     * Whether a user is currently authenticated.
     */
    val isAuthenticated: StateFlow<Boolean>

    /**
     * Initializes the auth repository.
     *
     * This should be called on app startup to:
     * - Check for stored session
     * - Restore session if valid
     * - Set initial auth state
     *
     * @return Result with authenticated User if session was restored, null otherwise
     */
    suspend fun initialize(): Result<User?>

    /**
     * Signs up a new user with email and password.
     *
     * On success:
     * - Stores session tokens securely
     * - Updates currentUser state
     *
     * @param email User's email address
     * @param password User's password
     * @return Result with [User] if successful, or error
     */
    suspend fun signUpWithEmail(email: String, password: String): Result<User>

    /**
     * Signs in an existing user with email and password.
     *
     * On success:
     * - Stores session tokens securely
     * - Updates currentUser state
     *
     * @param email User's email address
     * @param password User's password
     * @return Result with [User] if successful, or error
     */
    suspend fun signInWithEmail(email: String, password: String): Result<User>

    /**
     * Initiates OAuth sign-in flow with Discord.
     *
     * @return Result with OAuth URL to open in browser
     */
    suspend fun signInWithDiscord(): Result<String>

    /**
     * Initiates OAuth sign-in flow with Google.
     *
     * @return Result with OAuth URL to open in browser
     */
    suspend fun signInWithGoogle(): Result<String>

    /**
     * Completes OAuth sign-in after callback from provider.
     *
     * @param callbackUrl The deep link URL from OAuth provider
     * @return Result with [User] if successful, or error
     */
    suspend fun handleOAuthCallback(callbackUrl: String): Result<User>

    /**
     * Signs in with Apple using a native ID token.
     *
     * Used on iOS where we obtain the ID token from ASAuthorizationAppleIDCredential
     * and send it to Supabase for verification.
     *
     * @param idToken The identity token from Apple Sign In
     * @return Result with [User] if successful, or error
     */
    suspend fun signInWithAppleNative(idToken: String): Result<User>

    /**
     * Sends a password reset email to the given address.
     *
     * @param email User's email address
     * @return Result with success or error
     */
    suspend fun resetPasswordForEmail(email: String): Result<Unit>

    /**
     * Signs out the current user.
     *
     * - Clears session on server
     * - Removes stored tokens
     * - Updates currentUser to null
     *
     * @return Result with success or error
     */
    suspend fun signOut(): Result<Unit>

    /**
     * Refreshes the current session.
     *
     * Access tokens expire after 1 hour. This gets a new access token
     * using the refresh token.
     *
     * @return Result with updated User if successful, or error
     */
    suspend fun refreshSession(): Result<User>
}