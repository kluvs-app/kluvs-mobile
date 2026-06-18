package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.ServerDto
import com.ivangarzab.kluvs.api.models.ServerClubSummaryDto
import com.ivangarzab.kluvs.api.models.ServerGetSingleResponseDto
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.Server
import com.ivangarzab.kluvs.network.utils.parseDateOnlyString

/**
 * Maps a [ServerDto] from the API to a [Server] domain model.
 *
 * Used for the server list endpoint (`GET /server` without `id`) and the
 * create/update echo — in both cases nested clubs are the plain (lightweight)
 * shape with no `member_count`/`latest_session` (confirmed real backend
 * asymmetry vs. the single-server fetch, not a generator bug).
 */
fun ServerDto.toDomain(): Server {
    return Server(
        id = id,
        name = name,
        clubs = clubs?.map { it.toDomain() }
    )
}

/**
 * Maps a [ServerGetSingleResponseDto] (single-server fetch, `GET /server?id=`)
 * from the API to a [Server] domain model. Nested clubs here include
 * `member_count`/`latest_session`, unlike the list endpoint.
 */
fun ServerGetSingleResponseDto.toDomain(): Server {
    return Server(
        id = id ?: "",
        name = name ?: "",
        clubs = clubs?.map { it.toDomain() }
    )
}

/**
 * Maps a [ServerClubSummaryDto] (a club embedded within a
 * single-server response) to a [Club] domain model.
 *
 * Note: [Club.activeSession] is left null — `latest_session` here only carries
 * `id`/`due_date`/`books{title,author}` (no book id, no full session shape), so
 * a valid [com.ivangarzab.kluvs.model.Session] (which requires a non-null
 * [com.ivangarzab.kluvs.model.Book] with an id) cannot be constructed from it.
 * This is a real backend data gap, not something to paper over here — flag it
 * separately if the server list screen needs a real active session preview.
 */
fun ServerClubSummaryDto.toDomain(): Club {
    return Club(
        id = id,
        name = name,
        discordChannel = discordChannel,
        serverId = serverId,
        foundedDate = parseDateOnlyString(foundedDate),
        shameList = shameList?.map { it.toString() } ?: emptyList(),
        role = null,
        members = members?.map { it.toDomain() },
        activeSession = null,
        pastSessions = null
    )
}
