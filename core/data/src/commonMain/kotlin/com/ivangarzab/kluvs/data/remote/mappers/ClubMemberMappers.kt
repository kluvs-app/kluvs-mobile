package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.data.remote.dtos.ClubMemberDto
import com.ivangarzab.kluvs.model.ClubMember
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.network.utils.parseDateTimeString

/**
 * Maps a [ClubMemberDto] from the API to a [ClubMember] domain model.
 *
 * ClubMemberDto represents a member with their role in a specific club.
 * This is used in club responses where each member includes their club-specific role.
 */
fun ClubMemberDto.toDomain(): ClubMember {
    return ClubMember(
        role = Role.fromString(role),
        member = Member(
            id = id,
            name = name ?: "",
            handle = handle,
            avatarPath = avatar_path,
            booksRead = books_read,
            userId = user_id,
            createdAt = parseDateTimeString(created_at),
            clubs = null,
            shameClubs = null
        )
    )
}
