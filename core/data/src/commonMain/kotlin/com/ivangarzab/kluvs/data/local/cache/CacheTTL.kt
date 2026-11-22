package com.ivangarzab.kluvs.data.local.cache

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

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
     * Club data TTL: 15 minutes
     * Clubs embed dynamic nested data (activeSession, members) that changes often,
     * so this can't be as long-lived as the club's own static fields would allow.
     */
    val CLUB: Duration = 15.minutes

    /**
     * Member data TTL: 15 minutes
     * Member profiles update occasionally (avatar, points, books read).
     */
    val MEMBER: Duration = 15.minutes

    /**
     * Session data TTL: 15 minutes
     * Sessions are dynamic (active status, dates change).
     */
    val SESSION: Duration = 15.minutes

    /**
     * Book data TTL: 7 days
     * Book metadata is static (title, author, cover)
     */
    val BOOK: Duration = 7.days

    /**
     * Book enrichment data TTL: 7 days
     * Volume info and author info change rarely. Revisit if "More by this author" feels stale.
     */
    val BOOK_ENRICHMENT: Duration = 7.days

    /**
     * Shelf data TTL: 15 minutes
     * Shelf assignments are user-authored and change often; mutations
     * already reset freshness, so a short TTL keeps stale reads rare.
     */
    val SHELF: Duration = 15.minutes

    /**
     * Like data TTL: 15 minutes
     * Like state is user-authored and toggled frequently.
     */
    val LIKE: Duration = 15.minutes

    /**
     * Progress data TTL: 15 minutes
     * Reading progress updates frequently as the member reads.
     */
    val PROGRESS: Duration = 15.minutes

    /**
     * Discussion note data TTL: 15 minutes
     * Notes are user-authored and edited often.
     */
    val DISCUSSION_NOTE: Duration = 15.minutes

    /**
     * Discussion attendance data TTL: 15 minutes
     * RSVPs can change up until the discussion happens.
     */
    val DISCUSSION_ATTENDANCE: Duration = 15.minutes
}