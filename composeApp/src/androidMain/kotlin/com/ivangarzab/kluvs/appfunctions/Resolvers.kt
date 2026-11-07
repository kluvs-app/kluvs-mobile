package com.ivangarzab.kluvs.appfunctions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.Discussion
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.model.Session
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.toJavaLocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal data class ResolvedClub(val club: Club, val role: Role?, val activeSession: Session?)

internal data class DiscussionContext(val club: Club, val session: Session, val discussion: Discussion)

/** Exact ID, then exact name, then partial name — so "showcase" finds "Showcase Kluvs". */
internal fun <T> List<T>.matchByIdOrName(query: String, id: (T) -> String, name: (T) -> String): List<T> {
    val q = query.trim()
    return filter { id(it) == q }
        .ifEmpty { filter { name(it).equals(q, ignoreCase = true) } }
        .ifEmpty { filter { name(it).contains(q, ignoreCase = true) } }
}

/**
 * Resolves [club] (an ID or name) to one of the signed-in member's clubs. When [club] is
 * omitted, falls back to the user's only club with an active read; ambiguity is reported back
 * with the options rather than guessed at, so the agent can ask the user.
 */
internal suspend fun resolveClub(
    session: AppFunctionSession,
    clubRepository: ClubRepository,
    club: String?,
    needsActiveSession: Boolean,
): ResolvedClub {
    // Force-refreshed so the role used for role-gated actions is current.
    val memberClubs = session.requireMember(forceRefresh = true).clubs.orEmpty()
    if (memberClubs.isEmpty()) throw AppFunctionElementNotFoundException("You haven't joined any book clubs.")

    val query = club?.takeIf { it.isNotBlank() }
    val candidates = if (query == null) memberClubs else memberClubs.matchByIdOrName(query, { it.id }, { it.name })
    if (candidates.isEmpty()) {
        throw AppFunctionElementNotFoundException(
            "None of your clubs matches \"$query\". Your clubs: ${memberClubs.joinToString { it.name }}."
        )
    }

    val loaded = coroutineScope {
        candidates.map { candidate ->
            async {
                ResolvedClub(
                    club = clubRepository.getClub(candidate.id, forceRefresh = true).getOrThrowAppFunction(),
                    role = candidate.role,
                    activeSession = null,
                ).let { it.copy(activeSession = it.club.activeSession) }
            }
        }.awaitAll()
    }
    val eligible = if (needsActiveSession) loaded.filter { it.activeSession != null } else loaded

    return when {
        eligible.size == 1 -> eligible.single()
        eligible.isEmpty() && query == null ->
            throw AppFunctionElementNotFoundException("None of your clubs is reading a book right now.")
        eligible.isEmpty() ->
            throw AppFunctionElementNotFoundException(
                "${loaded.joinToString { it.club.name }} ${if (loaded.size == 1) "isn't" else "aren't"} reading a book right now."
            )
        query == null ->
            throw AppFunctionInvalidArgumentException(
                "Several of your clubs are reading right now: ${eligible.joinToString { it.club.name }}. Say which club."
            )
        else ->
            throw AppFunctionInvalidArgumentException(
                "\"$query\" matches several clubs: ${eligible.joinToString { it.club.name }}. Say which club."
            )
    }
}

/** Active sessions of the signed-in member's clubs, optionally narrowed to [clubId]. */
internal suspend fun activeSessions(
    session: AppFunctionSession,
    clubRepository: ClubRepository,
    forceRefresh: Boolean = false,
    clubId: String? = null,
): List<Pair<Club, Session>> = coroutineScope {
    session.requireMember().clubs.orEmpty()
        .filter { clubId == null || it.id == clubId }
        .map { club -> async { clubRepository.getClub(club.id, forceRefresh = forceRefresh).getOrThrowAppFunction() } }
        .awaitAll()
        .mapNotNull { club -> club.activeSession?.let { club to it } }
}

/**
 * Resolves [discussion] (an ID or title) among the user's clubs' active sessions — the only
 * discussions the app shows — optionally narrowed by [club]. When [discussion] is omitted, falls
 * back to the user's next upcoming discussion.
 */
@RequiresApi(36)
internal suspend fun resolveDiscussion(
    session: AppFunctionSession,
    clubRepository: ClubRepository,
    discussion: String?,
    club: String?,
): DiscussionContext {
    val clubFilter = club?.takeIf { it.isNotBlank() }
    val clubId = clubFilter?.let { resolveClub(session, clubRepository, it, needsActiveSession = true).club.id }
    val inClub = clubFilter?.let { " in $it" }.orEmpty()

    suspend fun candidates(forceRefresh: Boolean) = activeSessions(session, clubRepository, forceRefresh, clubId)
        .flatMap { (c, s) -> s.discussions.map { DiscussionContext(c, s, it) } }

    val query = discussion?.takeIf { it.isNotBlank() }
    if (query == null) {
        val today = LocalDate.now()
        return candidates(forceRefresh = false)
            .filter { !it.discussion.date.toJavaLocalDateTime().toLocalDate().isBefore(today) }
            .minByOrNull { it.discussion.date }
            ?: throw AppFunctionElementNotFoundException("You have no upcoming discussions$inClub.")
    }

    fun List<DiscussionContext>.match() = matchByIdOrName(query, { it.discussion.id }, { it.discussion.title })
    // Retry with a refresh so a just-created discussion isn't missed by a stale cache.
    val matches = candidates(forceRefresh = false).match().ifEmpty { candidates(forceRefresh = true).match() }
    return when (matches.size) {
        1 -> matches.single()
        0 -> throw AppFunctionElementNotFoundException("No discussion$inClub matches \"$query\".")
        else -> {
            val format = DateTimeFormatter.ofPattern("MMM d")
            throw AppFunctionInvalidArgumentException(
                "\"$query\" matches several discussions: " + matches.joinToString {
                    "${it.discussion.title} (${it.club.name}, ${it.discussion.date.toJavaLocalDateTime().format(format)})"
                } + ". Say which one."
            )
        }
    }
}
