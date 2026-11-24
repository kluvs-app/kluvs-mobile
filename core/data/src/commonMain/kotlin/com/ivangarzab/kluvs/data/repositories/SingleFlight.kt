package com.ivangarzab.kluvs.data.repositories

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Collapses concurrent calls for the same key into one: the first caller runs [run]'s block and
 * every caller that arrives while it is still running awaits that same result instead of
 * starting its own. Once the block finishes, the next call for the key starts fresh.
 *
 * Screens load several UseCases in parallel that all read the same club or member, so without
 * this one refresh sends several identical requests.
 *
 * The shared work runs in the first caller's coroutine. If that caller is cancelled (e.g. it
 * left the screen), the waiters are not: each one retries, and one of them becomes the new
 * first caller.
 */
internal class SingleFlight<K, V> {

    private val mutex = Mutex()
    private val inFlight = mutableMapOf<K, CompletableDeferred<V>>()

    suspend fun run(key: K, block: suspend () -> V): V {
        while (true) {
            val (deferred, isOwner) = mutex.withLock {
                inFlight[key]?.let { it to false }
                    ?: (CompletableDeferred<V>().also { inFlight[key] = it } to true)
            }

            if (isOwner) return runAsOwner(key, deferred, block)

            try {
                return deferred.await()
            } catch (e: CancellationException) {
                // Rethrows if this caller was cancelled; otherwise the owner was, so try again.
                currentCoroutineContext().ensureActive()
            }
        }
    }

    private suspend fun runAsOwner(key: K, deferred: CompletableDeferred<V>, block: suspend () -> V): V {
        try {
            return block().also { deferred.complete(it) }
        } catch (e: Throwable) {
            deferred.completeExceptionally(e)
            throw e
        } finally {
            withContext(NonCancellable) {
                mutex.withLock { if (inFlight[key] === deferred) inFlight.remove(key) }
            }
        }
    }
}
