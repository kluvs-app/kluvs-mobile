package com.ivangarzab.kluvs.auth.remote

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.auth.domain.EmailConfirmationRequiredException
import com.ivangarzab.kluvs.auth.domain.UserAlreadyExistsException
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Discord
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.ktor.http.Url
import io.ktor.http.parseQueryString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
            // Confirmation emails redirect back into the app via the same deep link OAuth uses
            // (kluvs://auth/callback) — without this, GoTrue falls back to the project's raw
            // site_url, which has no page to land on.
            val userInfo = auth.signUpWith(Email, redirectUrl = REDIRECT_URL) {
                this.email = email
                this.password = password
            }

            val session = auth.currentSessionOrNull()
            if (session != null) {
                Bark.i("User signed up and authenticated")
                return session
            }

            // GoTrue doesn't error when the email is already registered — to avoid leaking which
            // emails exist, it fakes a "success" (null session) indistinguishable from a genuine
            // pending-confirmation sign up, except that identities comes back empty instead of
            // containing the newly created identity.
            if (userInfo?.identities.isNullOrEmpty()) {
                throw UserAlreadyExistsException()
            }

            // Otherwise the Supabase project requires email confirmation: sign up succeeded but
            // no session is created until the user confirms via email — this is not a failure.
            throw EmailConfirmationRequiredException()
        } catch (e: UserAlreadyExistsException) {
            Bark.i("Email sign up rejected, account already exists")
            throw e
        } catch (e: EmailConfirmationRequiredException) {
            Bark.i("Email sign up succeeded, pending email confirmation")
            throw e
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

    override suspend fun updatePassword(newPassword: String) {
        Bark.v("Password update initiated")

        try {
            auth.updateUser {
                password = newPassword
            }
            Bark.i("Password updated")
        } catch (e: Exception) {
            Bark.e("Password update failed. User may need to retry.", e)
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

    override suspend fun restoreSession(): UserSession? = restoreMutex.withLock {
        Bark.v("Session restore started")

        // Normally Supabase is mid-restore here, loading its saved session at startup.
        var status = awaitSettledStatus(STARTUP_RESTORE_GRACE_MS)
        if (status == null && !auth.isAutoRefreshRunning) {
            // On Android, Supabase pauses the session whenever the app is backgrounded (status
            // back to Initializing, auto-refresh stopped) and only reloads it on the next
            // foreground start — so background callers must reload it themselves.
            Bark.d("Session paused; reloading it from storage")
            if (!auth.loadFromStorage()) {
                Bark.v("No saved session to restore")
                return@withLock null
            }
        }
        if (status == null) status = awaitSettledStatus(RESTORE_TIMEOUT_MS)

        when (status) {
            is SessionStatus.Authenticated -> status.session.also { Bark.d("Session restored") }
            is SessionStatus.NotAuthenticated -> null.also { Bark.v("No session to restore") }
            // Offline or slow network: keep the user signed in with the saved session while
            // Supabase keeps retrying the refresh in the background.
            is SessionStatus.RefreshFailure, SessionStatus.Initializing, null -> {
                Bark.w("Session refresh still pending (status: $status); using the saved session")
                auth.sessionManager.loadSession()
            }
        }
    }

    private suspend fun awaitSettledStatus(timeoutMs: Long): SessionStatus? =
        withTimeoutOrNull(timeoutMs) { auth.sessionStatus.first { it !is SessionStatus.Initializing } }

    override suspend fun retrieveCurrentUser(): UserInfo {
        Bark.v("Retrieving current user from server")

        try {
            return auth.retrieveUserForCurrentSession(updateSession = false)
        } catch (e: Exception) {
            // Warn, not error — callers use this to detect an already-deleted account, so a
            // failure here is often the expected outcome.
            Bark.w("Current user retrieval failed.", e)
            throw e
        }
    }

    // Concurrent restores (e.g. AppCoordinator and AuthViewModel at startup) must not both
    // reload a paused session, which would refresh twice with the same refresh token.
    private val restoreMutex = Mutex()

    companion object {
        const val REDIRECT_URL = "kluvs://auth/callback"
        private const val STARTUP_RESTORE_GRACE_MS = 1_000L
        private const val RESTORE_TIMEOUT_MS = 10_000L
    }
}