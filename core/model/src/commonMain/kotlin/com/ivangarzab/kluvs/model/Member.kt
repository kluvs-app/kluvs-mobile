package com.ivangarzab.kluvs.model

import kotlinx.datetime.LocalDateTime

/**
 * Domain model for the Member entity.
 *
 * Relations ([clubs], [shameClubs]) are nullable to support flexible loading:
 * - When fetched from API with expand, these are populated with full Club objects
 * - When fetched without expansion, these may be null
 */
data class Member(

    val id: String,

    val name: String,

    /** Username handle for this member. */
    val handle: String? = null,

    val avatarPath: String? = null,

    val booksRead: Int = 0,

    /** The User ID related to Auth. */
    val userId: String? = null,

    /** Timestamp when this member account was created. */
    val createdAt: LocalDateTime? = null,

    /**
     * List of [Club]s that this member belongs to.
     * Null when not loaded; empty list when loaded but member belongs to no clubs.
     */
    val clubs: List<Club>? = null,

    /**
     * List of [Club]s where this member is in the shame list.
     * Null when not loaded; empty list when loaded but member is not shamed anywhere.
     */
    val shameClubs: List<Club>? = null
)