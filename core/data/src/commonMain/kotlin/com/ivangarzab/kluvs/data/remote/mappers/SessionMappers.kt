package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.SessionDto
import com.ivangarzab.kluvs.api.models.SessionParticipantSummaryDto
import com.ivangarzab.kluvs.model.Session
import com.ivangarzab.kluvs.model.SessionMember

/**
 * Maps a [SessionDto] from the API to a [Session] domain model.
 *
 * Used both for the top-level `GET /session?id=` lookup and for sessions embedded
 * within Club/Member responses as `active_session` — both use this same generated shape.
 */
fun SessionDto.toDomain(): Session {
    return Session(
        id = id,
        clubId = clubId,
        book = book?.toDomain() ?: error("SessionDto missing required book data"),
        dueDate = dueDate?.parseDateString(),
        discussions = discussions?.map { it.toDomain() } ?: emptyList(),
        members = members?.map { it.toDomain() } ?: emptyList()
    )
}

/**
 * Maps a [SessionParticipantSummaryDto] from the club response's `active_session.members`
 * list to a [SessionMember] domain model.
 */
fun SessionParticipantSummaryDto.toDomain(): SessionMember {
    return SessionMember(
        memberId = memberId.toString(),
        memberName = memberName,
        isReading = isReading
    )
}
