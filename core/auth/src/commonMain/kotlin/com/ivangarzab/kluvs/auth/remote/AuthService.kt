package com.ivangarzab.kluvs.auth.remote

import io.github.jan.supabase.auth.user.UserSession

/**
 * Service for interacting with Supabase Auth.
 *
 * This is a thin wrapper around Supabase's GoTrue client that provides
 * auth operations. The repository layer will handle mapping to domain models
 * and managing secure storage.
 */
interface AuthService {

    /**
     * Signs up a new user with email and password.
     *
     * @param email User's email address
     * @param password User's password
     * @return UserSession containing access/refresh tokens and user info
     * @throws Exception if sign up fails (e.g., email already exists, weak password)
     */
    suspend fun signUpWithEmail(email: String, password: String): UserSession

    /**
     * Signs in an existing user with email and password.
     *
     * @param email User's email address
     * @param password User's password
     * @return [UserSession] containing access/refresh tokens and user info
     * @throws Exception if sign in fails (e.g., invalid credentials)
     */
    suspend fun signInWithEmail(email: String, password: String): UserSession

    /**
     * Initiates OAuth sign-in flow.
     *
     * For web-based OAuth (Discord, Google on Android without SDK), this returns
     * a URL that the app should open in a browser. The browser will redirect back
     * to the app via deep link with the auth code.
     *
     * @param provider OAuth provider (discord, google, apple)
     * @return OAuth URL to open in browser
     */
    suspend fun getOAuthUrl(provider: String): String

    /**
     * Completes OAuth sign-in after receiving callback from provider.
     *
     * This is called after the OAuth provider redirects back to the app.
     * The deep link will contain an auth code or token that we exchange
     * for a session.
     *
     * @param url The deep link URL received from OAuth callback
     * @return [UserSession] if OAuth was successful
     * @throws Exception if OAuth completion fails
     */
    suspend fun handleOAuthCallback(url: String): UserSession

    /**
     * Signs in with Apple using an ID token from native Apple Sign In.
     *
     * This is used on iOS where we get the identity token from
     * ASAuthorizationAppleIDCredential and send it to Supabase for verification.
     *
     * @param idToken The identity token from Apple Sign In
     * @return [UserSession] if sign in was successful
     * @throws Exception if sign in fails
     */
    suspend fun signInWithAppleIdToken(idToken: String): UserSession

    /**
     * Sends a password reset email to the given address.
     *
     * @param email User's email address
     * @throws Exception if the request fails (e.g., invalid email, rate limited)
     */
    suspend fun resetPasswordForEmail(email: String)

    /**
     * Signs out the current user.
     *
     * Invalidates the current session on the server.
     */
    suspend fun signOut()

    /**
     * Gets the current user session if one exists.
     *
     * @return Current UserSession, or null if not authenticated
     */
    suspend fun getCurrentSession(): UserSession?

    /**
     * Refreshes the current session using the refresh token.
     *
     * Access tokens expire after 1 hour, this gets a new access token.
     *
     * @return New UserSession with fresh access token
     * @throws Exception if refresh fails (e.g., refresh token expired)
     */
    suspend fun refreshSession(): UserSession

    /**
     * Restores a session from stored tokens.
     *
     * Used on app startup to restore a previous session without requiring
     * the user to sign in again.
     *
     * @param accessToken The stored access token
     * @param refreshToken The stored refresh token
     */
    suspend fun setSession(accessToken: String, refreshToken: String)
}