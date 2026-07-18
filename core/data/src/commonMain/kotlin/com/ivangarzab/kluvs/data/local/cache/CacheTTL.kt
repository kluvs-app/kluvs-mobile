package com.ivangarzab.kluvs.data.local.cache

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * Time-to-live (TTL) constants for cached data.
 * These values determine how long cached data is considered fresh before requiring a refresh.
 */
object CacheTTL {
    /**
     * Server data TTL: 7 days
     * Servers rarely change (name, icon, member count)
     */
    val SERVER: Duration = 7.days

    /**
     * Club data TTL: 24 hours
     * Clubs change infrequently (name, channel, founded date)
     */
    val CLUB: Duration = 24.hours

    /**
     * Member data TTL: 24 hours
     * Member profiles update occasionally (avatar, points, books read)
     */
    val MEMBER: Duration = 24.hours

    /**
     * Session data TTL: 6 hours
     * Sessions are more dynamic (active status, dates change)
     */
    val SESSION: Duration = 6.hours

    /**
     * Book data TTL: 7 days
     * Book metadata is static (title, author, cover)
     */
    val BOOK: Duration = 7.days

    /**
     * Shelf data TTL: 1 hour
     * Shelf assignments are user-authored and change often; mutations
     * already reset freshness, so a short TTL keeps stale reads rare.
     */
    val SHELF: Duration = 1.hours

    /**
     * Like data TTL: 1 hour
     * Like state is user-authored and toggled frequently.
     */
    val LIKE: Duration = 1.hours

    /**
     * Progress data TTL: 1 hour
     * Reading progress updates frequently as the member reads.
     */
    val PROGRESS: Duration = 1.hours

    /**
     * Discussion note data TTL: 1 hour
     * Notes are user-authored and edited often.
     */
    val DISCUSSION_NOTE: Duration = 1.hours

    /**
     * Discussion attendance data TTL: 1 hour
     * RSVPs can change up until the discussion happens.
     */
    val DISCUSSION_ATTENDANCE: Duration = 1.hours
}