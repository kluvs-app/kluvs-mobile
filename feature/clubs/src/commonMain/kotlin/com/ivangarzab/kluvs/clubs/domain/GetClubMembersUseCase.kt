package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.clubs.presentation.MemberListItemInfo
import com.ivangarzab.kluvs.data.repositories.AvatarRepository
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.ClubMember
import com.ivangarzab.kluvs.model.Role

/**
 * UseCase for fetching club members sorted by role for MembersTab.
 *
 * Transforms domain [ClubMember] models into UI-friendly [MemberListItemInfo] with:
 * - Member information
 * - Role for visual indicators
 * - Sorted by role (owners first, then admins, then members)
 *
 * @param clubRepository Repository for club data
 * @param avatarRepository Repository for avatar operations
 */
class GetClubMembersUseCase(
    private val clubRepository: ClubRepository,
    private val avatarRepository: AvatarRepository
) {
    /**
     * Fetches club members sorted by role.
     *
     * Members are returned sorted by role: owners first, then admins, then members.
     * Returns empty list if club has no members.
     *
     * @param clubId The ID of the club to retrieve members for
     * @return Result containing list of [MemberListItemInfo] if successful, or error if failed
     */
    suspend operator fun invoke(clubId: String, forceRefresh: Boolean = false): Result<List<MemberListItemInfo>> {
        Bark.d("Fetching club members (Club ID: $clubId)")
        return clubRepository.getClub(clubId, forceRefresh = forceRefresh).map { club: Club ->
            val memberItems = club.members
                ?.sortedBy { it.role.ordinal }  // Sort by role: OWNER (0), ADMIN (1), MEMBER (2)
                ?.map { clubMember: ClubMember ->
                    val member = clubMember.member
                    MemberListItemInfo(
                        memberId = member.id,
                        name = member.name,
                        handle = member.handle ?: "@",
                        avatarUrl = avatarRepository.getAvatarUrl(member.avatarPath),
                        role = clubMember.role,
                        userId = member.userId
                    )
                } ?: emptyList()
            Bark.i("Loaded club members (Count: ${memberItems.size})")
            memberItems
        }.onFailure { error ->
            Bark.e("Failed to fetch club members (Club ID: $clubId). User will see empty members list.", error)
        }
    }
}
