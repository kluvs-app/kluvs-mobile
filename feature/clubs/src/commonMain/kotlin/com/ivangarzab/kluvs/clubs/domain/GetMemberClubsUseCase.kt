package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.clubs.presentation.ClubListItem
import com.ivangarzab.kluvs.clubs.presentation.MemberAvatarInfo
import com.ivangarzab.kluvs.data.repositories.AvatarRepository
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.model.Club
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.time.ExperimentalTime

/**
 * UseCase for fetching the current user's [Club] list for the clubs list screen.
 *
 * Extracts the lightweight club list from the Member model, then enriches each
 * row in parallel with its active-session book title and member avatars
 * (mirrors web's `ClubsPage` per-club detail fetch). A row's enrichment failing
 * is non-fatal — it just falls back to name + role only.
 *
 * The resulting list keeps the order of `member.clubs` as returned by the backend
 * (soonest upcoming discussion first, then alphabetically by club name) — no
 * client-side re-sorting.
 *
 * @param memberRepository Repository for member data
 * @param clubRepository Repository for per-club detail data
 * @param avatarRepository Repository for avatar URL resolution
 */
@OptIn(ExperimentalTime::class)
class GetMemberClubsUseCase(
    private val memberRepository: MemberRepository,
    private val clubRepository: ClubRepository,
    private val avatarRepository: AvatarRepository
) {
    /**
     * Fetches all clubs for a member by their user ID.
     *
     * @param userId The auth user ID to look up
     * @param forceRefresh If true, bypasses cache for each club's row enrichment
     *        (e.g. pull-to-refresh) — the member fetch itself is always force-refreshed
     *        regardless (see below)
     * @return Result containing list of [com.ivangarzab.kluvs.clubs.presentation.ClubListItem], or error if failed
     */
    suspend operator fun invoke(userId: String, forceRefresh: Boolean = false): Result<List<ClubListItem>> {
        Bark.d("Fetching member clubs (User ID: $userId)")
        // Force-refresh: a cached Member's embedded clubs[].role can go stale after a role
        // change, silently hiding admin/owner actions (e.g. kebab menus) until the cache
        // TTL expires. Role correctness matters more than a cache hit here.
        return memberRepository.getMemberByUserId(userId, forceRefresh = true).map { member ->
            val clubs = member.clubs ?: emptyList()
            val clubItems = coroutineScope {
                clubs.map { club -> async { enrichClubListItem(club, forceRefresh) } }.map { it.await() }
            }
            Bark.i("Loaded member clubs (Count: ${clubItems.size})")
            clubItems
        }.onFailure { error ->
            Bark.e("Failed to fetch member clubs (User ID: $userId) and nothing was cached to fall back on.", error)
        }
    }

    private suspend fun enrichClubListItem(club: Club, forceRefresh: Boolean): ClubListItem {
        val fullClub = clubRepository.getClub(club.id, forceRefresh = forceRefresh).getOrElse {
            Bark.d("Failed to enrich club row (Club ID: ${club.id}); falling back to name + role only")
            null
        }
        return ClubListItem(
            id = club.id,
            name = club.name,
            role = club.role,
            bookTitle = fullClub?.activeSession?.book?.title,
            bookCoverUrl = fullClub?.activeSession?.book?.imageUrl,
            memberAvatarUrls = fullClub?.members.orEmpty().map { clubMember ->
                MemberAvatarInfo(
                    memberId = clubMember.member.id,
                    name = clubMember.member.name,
                    avatarUrl = avatarRepository.getAvatarUrl(clubMember.member.avatarPath)
                )
            },
            memberCount = fullClub?.members?.size ?: 0
        )
    }
}