package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.clubs.presentation.ClubListItem
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.model.Role

/**
 * UseCase for creating a new club.
 *
 * Unlike other club mutations, this has no existing membership/role to check against —
 * the creating member becomes the club's owner as a side effect of creation itself.
 */
class CreateClubUseCase(
    private val clubRepository: ClubRepository,
    private val memberRepository: MemberRepository
) {
    data class Params(val userId: String, val clubName: String)

    suspend operator fun invoke(params: Params): Result<ClubListItem> {
        Bark.d("Creating club (Name: ${params.clubName})")
        return memberRepository.getMemberByUserId(params.userId).mapCatching { member ->
            clubRepository.createClub(
                name = params.clubName,
                creatorMemberId = member.id,
                creatorMemberName = member.name,
                creatorBooksRead = member.booksRead
            ).getOrThrow()
        }.map { club ->
            ClubListItem(id = club.id, name = club.name, role = Role.OWNER)
        }.onSuccess {
            Bark.i("Club created (Club ID: ${it.id})")
        }.onFailure { error ->
            Bark.e("Failed to create club (Name: ${params.clubName}). Retry.", error)
        }
    }
}
