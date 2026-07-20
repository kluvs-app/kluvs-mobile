package com.ivangarzab.kluvs.model

import kotlinx.datetime.LocalDateTime

/**
 * Domain model for the Session entity.
 */
data class Session(

    val id: String,

    /** [Club] ID that this Session belongs to. **/
    val clubId: String,

    val book: Book,

    val dueDate: LocalDateTime?,

    /** List of [Discussion]s that this Session is related to. **/
    val discussions: List<Discussion> = emptyList(),

    /**
     * Participation list for this Session, with per-member reading flags.
     * Only populated when the session comes from the club response's
     * `active_session`; empty in other contexts.
     */
    val members: List<SessionMember> = emptyList()
)
