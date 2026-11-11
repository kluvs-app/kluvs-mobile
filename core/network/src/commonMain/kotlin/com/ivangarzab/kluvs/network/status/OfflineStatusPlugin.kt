package com.ivangarzab.kluvs.network.status

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import kotlin.coroutines.cancellation.CancellationException

private fun Throwable.hasTimeoutCause(): Boolean =
    generateSequence(this) { it.cause }.take(8).any { it is HttpRequestTimeoutException }

/** Configuration for [OfflineStatusPlugin]. */
internal class OfflineStatusPluginConfig {
    lateinit var status: OfflineStatus
}

/**
 * Ktor client plugin that reports network reachability to an [OfflineStatus].
 *
 * Hooks the send step, so it sees the engine's raw outcome before supabase-kt wraps failures
 * into its own exception type:
 * - a response of any status code → online
 * - an exception before any response (connect, DNS, TLS, timeout) → offline, then rethrown
 * - a timeout → offline (it surfaces as a cancellation with the timeout as its cause)
 * - any other cancellation → ignored, it says nothing about the network
 *
 * Observation only: it never changes a request, a response or an exception.
 */
internal val OfflineStatusPlugin = createClientPlugin("OfflineStatusPlugin", ::OfflineStatusPluginConfig) {
    val status = pluginConfig.status
    on(Send) { request ->
        try {
            proceed(request).also { status.markOnline() }
        } catch (e: CancellationException) {
            // Ktor's request timeout cancels the call with the timeout as the cause, so a
            // timeout arrives here as a cancellation. Any other cancellation (the user left the
            // screen) says nothing about the network.
            if (e.hasTimeoutCause()) status.markOffline()
            throw e
        } catch (e: Throwable) {
            status.markOffline()
            throw e
        }
    }
}
