package com.ivangarzab.kluvs.model

/**
 * Domain model for a discussion's attendance roster.
 */
data class AttendanceRoster(

    /** Members who have RSVP'd to the discussion. **/
    val responses: List<AttendanceResponse>,

    /** The caller's own RSVP, or null if they haven't answered yet. **/
    val myStatus: AttendanceStatus? = null,

    /** Total member count of the club, for "X of Y responded" displays. **/
    val totalMembers: Int
)

/**
 * A single member's RSVP within an [AttendanceRoster].
 */
data class AttendanceResponse(

    val memberId: String,

    val name: String? = null,

    val status: AttendanceStatus
)
