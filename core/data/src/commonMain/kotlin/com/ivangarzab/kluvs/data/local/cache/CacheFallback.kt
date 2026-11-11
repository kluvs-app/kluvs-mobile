package com.ivangarzab.kluvs.data.local.cache

import com.ivangarzab.bark.Bark

/**
 * Offline fallback for cached repositories: when the remote fetch failed, serve the cached
 * value (even past its TTL) if there is one, otherwise keep the original failure.
 *
 * Callers decide what counts as servable via [cached] (e.g. only a *complete* club, or a
 * non-empty list), and should read the cache lazily inside it so a successful fetch never
 * pays for the lookup.
 */
internal suspend inline fun <T> Result<T>.recoverWithCache(
    what: String,
    cached: () -> T?
): Result<T> {
    val error = exceptionOrNull() ?: return this
    val fallback = try {
        cached()
    } catch (e: Exception) {
        Bark.e("Failed to read cached $what after a remote failure.", e)
        null
    }
    return if (fallback != null) {
        Bark.w("Failed to fetch $what. Serving cached data instead.", error)
        Result.success(fallback)
    } else {
        Bark.e("Failed to fetch $what. No cached data available.", error)
        this
    }
}
