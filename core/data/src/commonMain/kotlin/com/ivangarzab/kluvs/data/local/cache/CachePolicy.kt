package com.ivangarzab.kluvs.data.local.cache

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.model.AppError
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/**
 * Cache policy for cached repository reads: decides when cached data is fresh (TTL), when a read
 * should skip the network, and how long a read with a cached fallback may wait for it.
 *
 * @param isOffline Whether the app already knows the network is unreachable (the
 *        `OfflineStatus` signal). Defaults to always online.
 * @param fallbackTimeout How long a read that has cached data to fall back on waits for the network
 */
@OptIn(ExperimentalTime::class)
class CachePolicy(
    private val clock: Clock = Clock.System,
    private val isOffline: () -> Boolean = { false },
    private val fallbackTimeout: Duration = FALLBACK_TIMEOUT
) {
    /**
     * Checks if cached data is stale based on its last fetched timestamp and TTL.
     *
     * @param lastFetchedAt Timestamp in milliseconds when data was last fetched (null = never fetched)
     * @param ttl Time-to-live duration for this data type
     * @return true if data is stale and should be refreshed, false if still fresh
     */
    fun isStale(lastFetchedAt: Long?, ttl: Duration): Boolean {
        // No timestamp = never cached, treat as stale
        if (lastFetchedAt == null) return true

        val now = clock.now().toEpochMilliseconds()
        val age = now - lastFetchedAt

        // Data is stale if older than TTL
        return age > ttl.inWholeMilliseconds
    }

    /**
     * Checks if cached data is fresh (opposite of stale).
     *
     * @param lastFetchedAt Timestamp in milliseconds when data was last fetched
     * @param ttl Time-to-live duration for this data type
     * @return true if data is fresh and can be used, false if should be refreshed
     */
    fun isFresh(lastFetchedAt: Long?, ttl: Duration): Boolean {
        return !isStale(lastFetchedAt, ttl)
    }

    /**
     * Whether a read should serve its cached value without trying the network: the app already
     * knows it is offline and there is something servable. A forced refresh (pull-to-refresh)
     * still tries the network, since the user explicitly asked for fresh data.
     */
    fun shouldServeCacheOnly(hasServableCache: Boolean, forceRefresh: Boolean): Boolean =
        hasServableCache && !forceRefresh && isOffline()

    /**
     * Runs a remote read. When the read has a cached value to fall back on, it is cut off after
     * [fallbackTimeout] and fails with [AppError.Timeout], so the caller serves the cache instead
     * of waiting out the HTTP client's own, much longer, timeout on a hung connection.
     *
     * The cut-off doesn't mark the app offline: a slow but working network must not flip the
     * "Working offline" banner. Requests without a fallback still run to the client's timeout.
     */
    suspend fun <T> fetchRemote(what: String, hasFallback: Boolean, fetch: suspend () -> Result<T>): Result<T> {
        if (!hasFallback) return fetch()
        return withTimeoutOrNull(fallbackTimeout) { fetch() } ?: run {
            // recoverWithCache logs the fallback itself at warn level
            Bark.d("Fetching $what took over $fallbackTimeout. Giving up on the network.")
            Result.failure(AppError.Timeout)
        }
    }

    companion object {
        /** Above the slowest healthy Edge Function call measured on device (about 2.2 s, KT-338). */
        val FALLBACK_TIMEOUT: Duration = 3.seconds
    }
}
