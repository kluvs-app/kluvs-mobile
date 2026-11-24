package com.ivangarzab.kluvs.data.local.cache

import com.ivangarzab.kluvs.model.AppError
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class CachePolicyTest {

    // Pinned so the test's "now" and CachePolicy's "now" can't drift apart mid-test
    private val fixedNow = Instant.fromEpochMilliseconds(1_750_000_000_000)
    private val cachePolicy = CachePolicy(clock = object : Clock {
        override fun now(): Instant = fixedNow
    })

    @Test
    fun testIsStale_withNullTimestamp_returnsTrue() {
        // Given
        val lastFetchedAt: Long? = null
        val ttl = 24.hours

        // When
        val result = cachePolicy.isStale(lastFetchedAt, ttl)

        // Then
        assertTrue(result, "Null timestamp should be considered stale")
    }

    @Test
    fun testIsStale_withFreshData_returnsFalse() {
        // Given - data fetched 1 hour ago
        val now = fixedNow.toEpochMilliseconds()
        val lastFetchedAt = now - 1.hours.inWholeMilliseconds
        val ttl = 24.hours

        // When
        val result = cachePolicy.isStale(lastFetchedAt, ttl)

        // Then
        assertFalse(result, "Data within TTL should not be stale")
    }

    @Test
    fun testIsStale_withStaleData_returnsTrue() {
        // Given - data fetched 25 hours ago
        val now = fixedNow.toEpochMilliseconds()
        val lastFetchedAt = now - 25.hours.inWholeMilliseconds
        val ttl = 24.hours

        // When
        val result = cachePolicy.isStale(lastFetchedAt, ttl)

        // Then
        assertTrue(result, "Data older than TTL should be stale")
    }

    @Test
    fun testIsStale_exactlyAtTTL_returnsFalse() {
        // Given - data fetched exactly 24 hours ago
        val now = fixedNow.toEpochMilliseconds()
        val lastFetchedAt = now - 24.hours.inWholeMilliseconds
        val ttl = 24.hours

        // When
        val result = cachePolicy.isStale(lastFetchedAt, ttl)

        // Then
        assertFalse(result, "Data exactly at TTL boundary should not be stale")
    }

    @Test
    fun testIsFresh_withNullTimestamp_returnsFalse() {
        // Given
        val lastFetchedAt: Long? = null
        val ttl = 24.hours

        // When
        val result = cachePolicy.isFresh(lastFetchedAt, ttl)

        // Then
        assertFalse(result, "Null timestamp should not be fresh")
    }

    @Test
    fun testIsFresh_withFreshData_returnsTrue() {
        // Given - data fetched 30 minutes ago
        val now = fixedNow.toEpochMilliseconds()
        val lastFetchedAt = now - 30.minutes.inWholeMilliseconds
        val ttl = 24.hours

        // When
        val result = cachePolicy.isFresh(lastFetchedAt, ttl)

        // Then
        assertTrue(result, "Data within TTL should be fresh")
    }

    @Test
    fun testIsFresh_withStaleData_returnsFalse() {
        // Given - data fetched 7 days ago
        val now = fixedNow.toEpochMilliseconds()
        val lastFetchedAt = now - (7 * 24).hours.inWholeMilliseconds
        val ttl = 24.hours

        // When
        val result = cachePolicy.isFresh(lastFetchedAt, ttl)

        // Then
        assertFalse(result, "Data older than TTL should not be fresh")
    }

    @Test
    fun testShouldServeCacheOnly_offlineWithCache_returnsTrue() {
        val offlinePolicy = CachePolicy(isOffline = { true })

        assertTrue(offlinePolicy.shouldServeCacheOnly(hasServableCache = true, forceRefresh = false))
    }

    @Test
    fun testShouldServeCacheOnly_offlineForcedRefresh_returnsFalse() {
        val offlinePolicy = CachePolicy(isOffline = { true })

        assertFalse(offlinePolicy.shouldServeCacheOnly(hasServableCache = true, forceRefresh = true))
    }

    @Test
    fun testShouldServeCacheOnly_offlineWithoutCache_returnsFalse() {
        val offlinePolicy = CachePolicy(isOffline = { true })

        assertFalse(offlinePolicy.shouldServeCacheOnly(hasServableCache = false, forceRefresh = false))
    }

    @Test
    fun testShouldServeCacheOnly_online_returnsFalse() {
        assertFalse(cachePolicy.shouldServeCacheOnly(hasServableCache = true, forceRefresh = false))
    }

    @Test
    fun testFetchRemote_withFallback_givesUpAfterFallbackTimeout() = runTest {
        val result = cachePolicy.fetchRemote("club", hasFallback = true) {
            delay(10.seconds)
            Result.success("fresh")
        }

        assertEquals(AppError.Timeout, result.exceptionOrNull())
        assertEquals(CachePolicy.FALLBACK_TIMEOUT.inWholeMilliseconds, testScheduler.currentTime)
    }

    @Test
    fun testFetchRemote_withFallback_returnsAFetchThatFinishesInTime() = runTest {
        val result = cachePolicy.fetchRemote("club", hasFallback = true) {
            delay(2.seconds)
            Result.success("fresh")
        }

        assertEquals("fresh", result.getOrNull())
    }

    @Test
    fun testFetchRemote_withoutFallback_waitsForTheFetch() = runTest {
        val result = cachePolicy.fetchRemote("club", hasFallback = false) {
            delay(10.seconds)
            Result.success("fresh")
        }

        assertEquals("fresh", result.getOrNull())
        assertEquals(10.seconds.inWholeMilliseconds, testScheduler.currentTime)
    }
}
