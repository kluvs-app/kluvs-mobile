package com.ivangarzab.kluvs.network.status

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConnectivityProbeTest {

    @Test
    fun noContentMeansReachable() = runTest {
        val probe = ConnectivityProbe(
            HttpClient(MockEngine { respond("", HttpStatusCode.NoContent) }),
            "https://probe.test/generate_204",
        )
        assertTrue(probe.isReachable())
    }

    @Test
    fun captivePortalOkPageIsNotReachable() = runTest {
        val probe = ConnectivityProbe(
            HttpClient(MockEngine { respond("<html>Sign in</html>", HttpStatusCode.OK) }),
            "https://probe.test/generate_204",
        )
        assertFalse(probe.isReachable())
    }

    @Test
    fun captivePortalRedirectIsNotReachable() = runTest {
        val probe = ConnectivityProbe(
            HttpClient(MockEngine {
                respond("", HttpStatusCode.Found, headersOf(HttpHeaders.Location, "https://portal.test/login"))
            }) { followRedirects = false },
            "https://probe.test/generate_204",
        )
        assertFalse(probe.isReachable())
    }

    @Test
    fun connectionFailureIsNotReachable() = runTest {
        val probe = ConnectivityProbe(
            HttpClient(MockEngine { throw RuntimeException("Unable to resolve host") }),
            "https://probe.test/generate_204",
        )
        assertFalse(probe.isReachable())
    }

    @Test
    fun cancellationIsNotSwallowed() = runTest {
        val probe = ConnectivityProbe(
            HttpClient(MockEngine { throw CancellationException("Cancelled") }),
            "https://probe.test/generate_204",
        )
        assertFailsWith<CancellationException> { probe.isReachable() }
    }
}
