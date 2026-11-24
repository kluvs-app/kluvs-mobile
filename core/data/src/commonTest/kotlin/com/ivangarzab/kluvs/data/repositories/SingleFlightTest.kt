package com.ivangarzab.kluvs.data.repositories

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SingleFlightTest {

    private val singleFlight = SingleFlight<String, Int>()

    @Test
    fun `concurrent calls for the same key share one run`() = runTest {
        var runs = 0

        val results = List(3) {
            async { singleFlight.run("club-1") { runs++; delay(100); 42 } }
        }.awaitAll()

        assertEquals(listOf(42, 42, 42), results)
        assertEquals(1, runs)
    }

    @Test
    fun `concurrent calls for different keys run separately`() = runTest {
        var runs = 0

        val results = listOf("club-1", "club-2").map { key ->
            async { singleFlight.run(key) { runs++; delay(100); key.length } }
        }.awaitAll()

        assertEquals(listOf(6, 6), results)
        assertEquals(2, runs)
    }

    @Test
    fun `a call after the previous run finished starts a new run`() = runTest {
        var runs = 0

        singleFlight.run("club-1") { runs++; 1 }
        val second = singleFlight.run("club-1") { runs++; 2 }

        assertEquals(2, second)
        assertEquals(2, runs)
    }

    @Test
    fun `a failure is shared with every waiting caller`() = runTest {
        val calls = List(2) {
            async {
                runCatching { singleFlight.run("club-1") { delay(100); throw IllegalStateException("boom") } }
            }
        }.awaitAll()

        calls.forEach { assertFailsWith<IllegalStateException> { it.getOrThrow() } }
    }

    @Test
    fun `a waiter takes over when the first caller is cancelled`() = runTest {
        var runs = 0
        val ownerStarted = CompletableDeferred<Unit>()

        val owner = launch {
            singleFlight.run("club-1") { runs++; ownerStarted.complete(Unit); delay(1_000); 1 }
        }
        ownerStarted.await()
        val waiter = async { singleFlight.run("club-1") { runs++; delay(100); 2 } }
        delay(10)
        owner.cancel()

        assertEquals(2, waiter.await())
        assertEquals(2, runs)
    }
}
