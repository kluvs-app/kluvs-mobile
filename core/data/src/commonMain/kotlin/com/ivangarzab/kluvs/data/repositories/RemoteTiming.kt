package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.bark.Bark
import kotlin.time.measureTimedValue

/**
 * Runs a remote read and logs how long it took, so slow screens can be attributed to network
 * latency versus app work (KT-338). A failure is timed too: an offline read that waits out the
 * request timeout shows up here as a long failed fetch.
 */
internal inline fun <T> timedRemote(what: String, fetch: () -> Result<T>): Result<T> {
    val (result, elapsed) = measureTimedValue(fetch)
    val outcome = if (result.isSuccess) "Fetched" else "Failed to fetch"
    Bark.d("$outcome $what from remote in ${elapsed.inWholeMilliseconds} ms")
    return result
}
