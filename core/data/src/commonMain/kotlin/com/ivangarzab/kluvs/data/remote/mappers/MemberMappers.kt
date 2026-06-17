package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.data.remote.dtos.MemberDto
import com.ivangarzab.kluvs.data.remote.dtos.MemberResponseDto
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.network.utils.parseDateTimeString

/**
 * Maps a [com.ivangarzab.kluvs.data.remote.dtos.MemberDto] from the API to a [Member] domain model.
 *
 * Note: MemberDto contains only basic member info without nested Club objects.
 * Relations (clubs, shameClubs) will be null.
 */
fun MemberDto.toDomain(): Member {
    return Member(
        id = id,
        name = name ?: "",  // Provide default if backend doesn't return name
        handle = handle,
        avatarPath = avatar_path,
        booksRead = books_read,
        userId = user_id,
        createdAt = parseDateTimeString(created_at),
        clubs = null,
        shameClubs = null
    )
}

/**
 * Maps a [com.ivangarzab.kluvs.data.remote.dtos.MemberResponseDto] from the API to a [Member] domain model.
 *
 * This is the full member response with all nested relations populated:
 * - clubs (list of Club objects the member belongs to)
 * - shame_clubs (list of Club objects where member is shamed)
 */
fun MemberResponseDto.toDomain(): Member {
    return Member(
        id = id,
        name = name,
        handle = handle,
        avatarPath = avatar_path,
        booksRead = books_read,
        userId = user_id,
        createdAt = parseDateTimeString(created_at),
        // Map nested ClubDto objects to domain models
        clubs = clubs.map { it.toDomain() },
        shameClubs = shame_clubs.map { it.toDomain() }
    )
}
