package com.ivangarzab.kluvs.network.status

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@OptIn(SupabaseInternal::class)
class OfflineStatusPluginTest {

    private val status = OfflineStatus()

    private fun client(handler: suspend () -> Unit = {}, respondWith: () -> Int = { 200 }): HttpClient =
        HttpClient(MockEngine {
            handler()
            respond("{}", HttpStatusCode.fromValue(respondWith()))
        }) {
            expectSuccess = false
            install(OfflineStatusPlugin) { status = this@OfflineStatusPluginTest.status }
        }

    private fun failingClient(error: Throwable): HttpClient =
        HttpClient(MockEngine { throw error }) {
            install(OfflineStatusPlugin) { status = this@OfflineStatusPluginTest.status }
        }

    @Test
    fun startsOnline() {
        assertFalse(status.isOffline.value)
    }

    @Test
    fun successfulResponseKeepsOnline() = runTest {
        client().get("https://example.test/ok")
        assertFalse(status.isOffline.value)
    }

    @Test
    fun serverErrorCountsAsReachable() = runTest {
        status.markOffline()
        client(respondWith = { 500 }).get("https://example.test/boom")
        assertFalse(status.isOffline.value)
    }

    @Test
    fun clientErrorCountsAsReachable() = runTest {
        status.markOffline()
        client(respondWith = { 404 }).get("https://example.test/missing")
        assertFalse(status.isOffline.value)
    }

    @Test
    fun connectionFailureMarksOfflineAndRethrows() = runTest {
        val failure = RuntimeException("Unable to resolve host")

        val thrown = assertFailsWith<RuntimeException> {
            failingClient(failure).get("https://example.test/ok")
        }

        assertEquals(failure.message, thrown.message)
        assertTrue(status.isOffline.value)
    }

    @Test
    fun nextSuccessfulResponseRestoresOnline() = runTest {
        assertFailsWith<RuntimeException> {
            failingClient(RuntimeException("No route")).get("https://example.test/ok")
        }
        assertTrue(status.isOffline.value)

        client().get("https://example.test/ok")

        assertFalse(status.isOffline.value)
    }

    @Test
    fun cancellationDoesNotMarkOffline() = runTest {
        assertFailsWith<CancellationException> {
            failingClient(CancellationException("Cancelled")).get("https://example.test/ok")
        }
        assertFalse(status.isOffline.value)
    }

    @Test
    fun supabaseFunctionCallFailureMarksOffline() = runTest {
        val supabase = createSupabaseClient(
            supabaseUrl = "https://example.supabase.co",
            supabaseKey = "test-key",
        ) {
            httpEngine = MockEngine { throw RuntimeException("Unable to resolve host") }
            install(Functions)
            httpConfig { install(OfflineStatusPlugin) { status = this@OfflineStatusPluginTest.status } }
        }

        assertFailsWith<Exception> { supabase.functions.invoke("club") }

        assertTrue(status.isOffline.value)
    }

    @Test
    fun supabaseFunctionCallResponseKeepsOnline() = runTest {
        status.markOffline()
        val supabase = createSupabaseClient(
            supabaseUrl = "https://example.supabase.co",
            supabaseKey = "test-key",
        ) {
            httpEngine = MockEngine { respondError(HttpStatusCode.InternalServerError) }
            install(Functions)
            httpConfig { install(OfflineStatusPlugin) { status = this@OfflineStatusPluginTest.status } }
        }

        assertFailsWith<Exception> { supabase.functions.invoke("club") }

        assertFalse(status.isOffline.value)
    }

    @Test
    fun supabaseRequestTimeoutMarksOffline() = runTest {
        val supabase = createSupabaseClient(
            supabaseUrl = "https://example.supabase.co",
            supabaseKey = "test-key",
        ) {
            requestTimeout = 100.milliseconds
            httpEngine = MockEngine {
                delay(5_000)
                respond("{}")
            }
            install(Functions)
            httpConfig { install(OfflineStatusPlugin) { status = this@OfflineStatusPluginTest.status } }
        }

        // Real dispatcher so the request timeout runs on the wall clock, not virtual time
        withContext(Dispatchers.Default) {
            assertFailsWith<Exception> { supabase.functions.invoke("club") }
        }

        assertTrue(status.isOffline.value)
    }
}
