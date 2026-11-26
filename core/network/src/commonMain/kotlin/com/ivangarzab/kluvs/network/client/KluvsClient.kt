package com.ivangarzab.kluvs.network.client

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import org.koin.core.module.Module

/** Header the backend reads to tell which client app, and which release of it, made a request. */
internal const val KLUVS_CLIENT_HEADER = "X-Kluvs-Client"

/**
 * The installed app, sent on every request as `X-Kluvs-Client: <platform>/<version>`.
 *
 * @property platform exactly `android` or `ios`.
 * @property version the human-readable release version (MAJOR.MINOR.PATCH, same as the release
 * tag), never a build number.
 */
internal data class KluvsClient(val platform: String, val version: String) {
    val headerValue: String get() = "$platform/$version"
}

/** Provides [KluvsClient], read from the installed app at runtime (platform-specific). */
internal expect val kluvsClientModule: Module

/** Adds the [KLUVS_CLIENT_HEADER] header to every request this client makes. */
internal fun HttpClientConfig<*>.sendKluvsClientHeader(client: KluvsClient) {
    defaultRequest { header(KLUVS_CLIENT_HEADER, client.headerValue) }
}
