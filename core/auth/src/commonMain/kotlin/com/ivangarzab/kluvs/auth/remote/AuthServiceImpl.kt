package com.ivangarzab.kluvs.auth.remote

import com.ivangarzab.bark.Bark
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Discord
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import io.ktor.http.Url
import io.ktor.http.parseQueryString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Implementation of [AuthService] using Supabase GoTrue.
 */
class AuthServiceImpl(
    private val supabaseClient: SupabaseClient
) : AuthService {

    private val auth: Auth
        get() = supabaseClient.auth

    override suspend fun signUpWithEmail(email: String, password: String): UserSession {
        Bark.v("Email sign up initiated")

        return try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            // After sign up, user is automatically signed in
            val session = auth.currentSessionOrNull()
                ?: throw IllegalStateException("Sign up succeeded but no session was created")

            Bark.i("User signed up and authenticated")
            session
        } catch (e: Exception) {
            Bark.e("Email sign up failed. User may need to retry.", e)
            throw e
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): UserSession {
        Bark.v("Email sign in initiated")

        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val session = auth.currentSessionOrNull()
                ?: throw IllegalStateException("Sign in succeeded but no session was created")

            Bark.i("User signed in")
            session
        } catch (e: Exception) {
            Bark.e("Email sign in failed. Check credentials and retry.", e)
            throw e
        }
    }

    override suspend fun getOAuthUrl(provider: String): String {
        Bark.v("Generating OAuth URL for provider: $provider")

        return try {
            val oAuthProvider = when (provider.lowercase()) {
                "discord" -> Discord
                "google" -> Google
                "apple" -> Apple
                else -> throw IllegalArgumentException("Unknown OAuth provider: $provider")
            }

            val url = auth.getOAuthUrl(oAuthProvider, redirectUrl = REDIRECT_URL)

            Bark.d("OAuth URL generated")
            url
        } catch (e: Exception) {
            Bark.e("Failed to generate OAuth URL. OAuth sign in will not be available.", e)
            throw e
        }
    }

    override suspend fun handleOAuthCallback(url: String): UserSession {
        Bark.d("Processing OAuth callback")

        return try {
            // Parse the callback URL to extract tokens from the fragment
            // Supabase OAuth callbacks use the fragment (after #) for token data
            val parsedUrl = Url(url)
            val fragment = parsedUrl.fragment

            if (fragment.isBlank()) {
                Bark.w("OAuth callback missing token data. Callback URL is malformed.")
                throw IllegalArgumentException("OAuth callback URL missing fragment with tokens")
            }

            // Parse fragment parameters (format: access_token=...&refresh_token=...&...)
            val params = parseQueryString(fragment)
            val accessToken = params["access_token"]
                ?: throw IllegalArgumentException("Missing access_token in OAuth callback")
            val refreshToken = params["refresh_token"]
                ?: throw IllegalArgumentException("Missing refresh_token in OAuth callback")

            Bark.v("Parsed OAuth tokens from callback")

            // Import the tokens and retrieve user data to establish complete session
            auth.importAuthToken(
                accessToken = accessToken,
                refreshToken = refreshToken,
                retrieveUser = true
            )

            // Wait for session status to confirm authentication (with timeout)
            // This handles the race condition where currentSessionOrNull() returns null
            // before the session is fully established internally
            Bark.v("Awaiting session confirmation (5s timeout)")
            val sessionResult = withTimeoutOrNull(5000L) {
                auth.sessionStatus.first { status ->
                    Bark.v("Session status: $status")
                    status is SessionStatus.Authenticated
                }
            }

            if (sessionResult == null) {
                Bark.e("OAuth session confirmation timed out after 5 seconds. Session may not be established.", null)
                throw IllegalStateException("OAuth timeout: session not established")
            }

            val userSession = auth.currentSessionOrNull()
                ?: throw IllegalStateException("OAuth succeeded but no session was created")

            Bark.i("User authenticated via OAuth")
            userSession
        } catch (e: Exception) {
            Bark.e("OAuth callback processing failed. User will need to retry OAuth sign in.", e)
            throw e
        }
    }

    override suspend fun signInWithAppleIdToken(idToken: String): UserSession {
        Bark.v("Apple ID token sign in initiated")

        return try {
            auth.signInWith(IDToken) {
                this.idToken = idToken
                this.provider = Apple
            }

            val session = auth.currentSessionOrNull()
                ?: throw IllegalStateException("Apple Sign In succeeded but no session was created")

            Bark.i("User authenticated via Apple ID")
            session
        } catch (e: Exception) {
            Bark.e("Apple ID sign in failed. Please retry.", e)
            throw e
        }
    }

    override suspend fun resetPasswordForEmail(email: String) {
        Bark.v("Password reset email requested")

        try {
            auth.resetPasswordForEmail(email)
            Bark.i("Password reset email sent")
        } catch (e: Exception) {
            Bark.e("Password reset email failed to send. User may need to retry.", e)
            throw e
        }
    }

    override suspend fun signOut() {
        Bark.v("Sign out initiated")

        try {
            auth.signOut()

            // Wait for session status to confirm sign out (with timeout)
            // This ensures the session is fully cleared before returning
            Bark.v("Awaiting session confirmation (3s timeout)")
            val signOutResult = withTimeoutOrNull(3000L) {
                auth.sessionStatus.first { status ->
                    Bark.v("Session status: $status")
                    status is SessionStatus.NotAuthenticated
                }
            }

            if (signOutResult == null) {
                Bark.w("Sign out session confirmation timed out after 3 seconds. Proceeding anyway.")
            }

            Bark.i("User signed out")
        } catch (e: Exception) {
            Bark.e("Sign out failed. User session may still be active.", e)
            throw e
        }
    }

    override suspend fun getCurrentSession(): UserSession? {
        return auth.currentSessionOrNull()
    }

    override suspend fun refreshSession(): UserSession {
        Bark.v("Session refresh initiated")

        return try {
            auth.refreshCurrentSession()
            val session = auth.currentSessionOrNull()
                ?: throw IllegalStateException("Session refresh succeeded but no session exists")

            Bark.d("Session refreshed")
            session
        } catch (e: Exception) {
            Bark.e("Session refresh failed. User may need to re-authenticate.", e)
            throw e
        }
    }

    override suspend fun setSession(accessToken: String, refreshToken: String) {
        Bark.v("Restoring session from stored tokens")

        try {
            auth.importAuthToken(
                accessToken = accessToken,
                refreshToken = refreshToken,
                retrieveUser = true
            )
            Bark.d("Session restored")
        } catch (e: Exception) {
            Bark.e("Session restoration failed. Stored credentials may be invalid.", e)
            throw e
        }
    }

    companion object {
        const val REDIRECT_URL = "kluvs://auth/callback"
    }
}