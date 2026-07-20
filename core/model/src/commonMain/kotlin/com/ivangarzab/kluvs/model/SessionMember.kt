package com.ivangarzab.kluvs.model

/**
 * Domain model for a member's participation in a reading [Session].
 *
 * Populated from the club response's `active_session.members` list.
 */
data class SessionMember(

    /** [Member] ID of the participant. **/
    val memberId: String,

    /** Display name of the participant, when provided by the API. **/
    val memberName: String? = null,

    /** Whether the member is actively reading (vs. opted out of the session). **/
    val isReading: Boolean = true
)
