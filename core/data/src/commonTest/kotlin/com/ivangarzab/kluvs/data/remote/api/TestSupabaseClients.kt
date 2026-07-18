package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.kluvs.network.BuildKonfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain

/**
 * Seeded auth user from /kluvs-backend/supabase/seed.sql — Member #1 (Ivan Garza).
 * The member row is auto-created by the on_auth_user_created trigger and enriched
 * by the seed, so this account always maps to member_id 1.
 */
const val TEST_USER_EMAIL = "ivan@example.com"
const val TEST_USER_PASSWORD = "12345678"
const val TEST_USER_MEMBER_ID = 1

/**
 * Creates a [SupabaseClient] signed in as the seeded test user.
 *
 * Member-scoped endpoints (shelf, like, progress, discussion-note,
 * discussion-attendance, join, session reading log) resolve the member from the
 * caller's JWT and reject bot (service-role) callers with 403 — so integration
 * tests for those endpoints must use this client instead of a bare one.
 */
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun createUserAuthedSupabaseClient(): SupabaseClient {
    // The Auth plugin dispatches session events on Dispatchers.Main, which has no
    // platform implementation on the test JVM — substitute a test dispatcher.
    Dispatchers.setMain(UnconfinedTestDispatcher())

    val client = createSupabaseClient(
        supabaseUrl = BuildKonfig.TEST_SUPABASE_URL,
        supabaseKey = BuildKonfig.TEST_SUPABASE_KEY,
    ) {
        install(Auth) {
            // In-memory storage: the test JVM has no multiplatform-settings backing
            sessionManager = MemorySessionManager()
            codeVerifierCache = MemoryCodeVerifierCache()
            autoLoadFromStorage = false
            alwaysAutoRefresh = false
            // The android variant registers ProcessLifecycleOwner observers, which
            // needs a main Looper the unit-test JVM doesn't have
            enableLifecycleCallbacks = false
        }
        install(Functions)
    }
    client.auth.signInWith(Email) {
        email = TEST_USER_EMAIL
        password = TEST_USER_PASSWORD
    }
    return client
}

/**
 * Creates a bot-authenticated (service role key) [SupabaseClient] — the same
 * shape the other integration tests build inline.
 */
fun createBotSupabaseClient(): SupabaseClient = createSupabaseClient(
    supabaseUrl = BuildKonfig.TEST_SUPABASE_URL,
    supabaseKey = BuildKonfig.TEST_SUPABASE_KEY,
) {
    install(Functions)
}
