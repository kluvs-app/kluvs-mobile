package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.ClubMemberDto
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
        role = role?.let { Role.fromString(it.value) } ?: Role.MEMBER,
        member = Member(
            id = id?.toString() ?: "",
            name = name ?: "",
            handle = handle,
            avatarPath = avatarPath,
            booksRead = booksRead ?: 0,
            userId = null, // Not present on this embedded wrapper
            createdAt = parseDateTimeString(createdAt),
            clubs = null,
            shameClubs = null
        )
    )
}
