package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.MemberDto
import com.ivangarzab.kluvs.api.models.MemberGetResponseDto
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.network.utils.parseDateTimeString

/**
 * Maps a [MemberDto] from the API to a [Member] domain model.
 *
 * Note: MemberDto contains only basic member info without nested Club objects.
 * Relations (clubs, shameClubs) will be null.
 */
fun MemberDto.toDomain(): Member {
    return Member(
        id = id.toString(),
        name = name,
        handle = handle,
        avatarPath = avatarPath,
        booksRead = booksRead ?: 0,
        userId = userId,
        createdAt = parseDateTimeString(createdAt),
        clubs = null,
        shameClubs = null
    )
}

/**
 * Maps a [MemberGetResponseDto] from the API to a [Member] domain model.
 *
 * This is the full member response with all nested relations populated:
 * - clubs (list of Club objects the member belongs to, each with its per-club role)
 * - shame_clubs (list of Club objects where member is shamed)
 */
fun MemberGetResponseDto.toDomain(): Member {
    return Member(
        id = id.toString(),
        name = name,
        handle = handle,
        avatarPath = avatarPath,
        booksRead = booksRead ?: 0,
        userId = userId,
        createdAt = parseDateTimeString(createdAt),
        clubs = clubs?.map { it.toDomain() } ?: emptyList(),
        shameClubs = shameClubs?.map { it.toDomain() } ?: emptyList()
    )
}
