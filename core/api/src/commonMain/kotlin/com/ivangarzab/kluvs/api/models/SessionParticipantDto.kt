/**
 * HAND-WRITTEN — not produced by the OpenAPI generator pipeline.
 *
 * The club GET response embeds `active_session.members[]` entries shaped as
 * `{member_id, member_name, is_reading}`, but the OpenAPI spec's `Session`
 * schema does not declare that field yet, so the generator never emits this
 * shape. The generated [SessionMemberDto] cannot be reused here because it
 * marks `session_id` as required while the club response omits it.
 *
 * Delete this class (and the matching hand-edit in [SessionDto]) once the
 * canonical spec gains `Session.members` and the models are regenerated.
 */

package com.ivangarzab.kluvs.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A member's participation entry on a session, as embedded in the club
 * response's `active_session.members` array.
 *
 * @param memberId
 * @param memberName
 * @param isReading Whether the member is actively reading (vs. opted out)
 */
@Serializable
data class SessionParticipantDto(

    @SerialName(value = "member_id") val memberId: Int,

    @SerialName(value = "member_name") val memberName: String? = null,

    /* Whether the member is actively reading (vs. opted out) */
    @SerialName(value = "is_reading") val isReading: Boolean = true
)
