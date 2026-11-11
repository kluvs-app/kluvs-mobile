package com.ivangarzab.kluvs.network.status

import com.ivangarzab.bark.Bark
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlin.coroutines.cancellation.CancellationException

/** Answers whether the internet is currently reachable. */
internal fun interface ConnectivityCheck {
    suspend fun isReachable(): Boolean
}

/**
 * Checks whether the internet is reachable, with one tiny request to a connectivity-check endpoint.
 *
 * Used only to notice that the network has come back while the app is offline, since nothing else
 * makes a request at that point. It deliberately does not go through the Supabase client or any
 * Edge Function: it is a plain request to Google's `generate_204` (the check Android itself uses),
 * which answers an empty 204. Requiring exactly 204 means a captive portal, which answers 200 or
 * a redirect to its login page, is not mistaken for a working connection.
 *
 * It proves the internet is reachable, not that our backend is; backend failures are still
 * picked up by [OfflineStatusPlugin] on real requests.
 */
internal class ConnectivityProbe(
    private val client: HttpClient = defaultClient(),
    private val url: String = PROBE_URL,
) : ConnectivityCheck {

    override suspend fun isReachable(): Boolean = try {
        client.get(url).status == HttpStatusCode.NoContent
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Bark.v("Network: Connectivity probe failed (${e::class.simpleName})")
        false
    }

    companion object {
        const val PROBE_URL = "https://connectivitycheck.gstatic.com/generate_204"
        private const val PROBE_TIMEOUT_MS = 5_000L

        private fun defaultClient() = HttpClient {
            followRedirects = false
            expectSuccess = false
            install(HttpTimeout) { requestTimeoutMillis = PROBE_TIMEOUT_MS }
        }
    }
}
