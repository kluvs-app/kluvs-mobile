package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.DiscussionDto
import com.ivangarzab.kluvs.api.models.SessionDiscussionInputDto
import com.ivangarzab.kluvs.model.Discussion

/**
 * Maps a [com.ivangarzab.kluvs.api.models.DiscussionDto] from the API to a [Discussion] domain model.
 */
fun DiscussionDto.toDomain(): Discussion {
    return Discussion(
        id = id,
        sessionId = sessionId,
        title = title,
        date = scheduledAt.parseDateString(),
        location = location
    )
}

/**
 * Maps a [Discussion] domain model to a [SessionDiscussionInputDto] request payload.
 */
fun Discussion.toDto(): SessionDiscussionInputDto = SessionDiscussionInputDto(
    title = title,
    scheduledAt = date.toString(),
    location = location
)
