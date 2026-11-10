package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.kluvs.data.local.mappers.toDomain
import com.ivangarzab.kluvs.data.local.mappers.toEntity
import com.ivangarzab.kluvs.database.KluvsDatabase
import com.ivangarzab.kluvs.database.entities.ClubMemberCrossRef
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.ClubMember
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.bark.Bark

/**
 * Local data source for Club entities.
 * Handles CRUD operations with the local Room database.
 */
interface ClubLocalDataSource {
    suspend fun getClub(clubId: String): Club?
    suspend fun getClubsForServer(serverId: String): List<Club>
    suspend fun insertClub(club: Club)
    suspend fun insertClubs(clubs: List<Club>)
    suspend fun deleteClub(clubId: String)

    /** Keeps the cached club but forces the next non-forced read to refetch it. */
    suspend fun markStale(clubId: String)
    suspend fun getLastFetchedAt(clubId: String): Long?
    suspend fun deleteAll()
}

/**
 * Implementation of [ClubLocalDataSource] using Room database.
 */
class ClubLocalDataSourceImpl(
    private val database: KluvsDatabase
) : ClubLocalDataSource {

    private val clubDao = database.clubDao()
    private val memberDao = database.memberDao()
    private val sessionDao = database.sessionDao()
    private val bookDao = database.bookDao()
    private val discussionDao = database.discussionDao()

    override suspend fun getClub(clubId: String): Club? {
        val clubEntity = clubDao.getClub(clubId) ?: return null

        return try {
            // Load members for this club with their roles
            val memberEntities = memberDao.getMembersForClub(clubId)
            val members = if (memberEntities.isNotEmpty()) {
                val crossRefs = memberDao.getClubMemberCrossRefsForClub(clubId)
                val roleMap = crossRefs.associate { it.memberId to it.role }
                memberEntities.map { memberEntity ->
                    ClubMember(
                        role = Role.fromString(roleMap[memberEntity.id] ?: "member"),
                        member = memberEntity.toDomain()
                    )
                }
            } else null

            // Load active session for this club (get the most recent session)
            val sessionEntities = sessionDao.getSessionsForClub(clubId)
            val activeSession = sessionEntities.firstOrNull()?.let { sessionEntity ->
                val bookId = sessionEntity.bookId ?: return@let null
                val bookEntity = bookDao.getBook(bookId) ?: return@let null
                val discussions = discussionDao.getDiscussionsForSession(sessionEntity.id).map { it.toDomain() }
                val sessionMembers = sessionDao.getSessionMembers(sessionEntity.id).map { it.toDomain() }
                sessionEntity.toDomain(bookEntity.toDomain()).copy(
                    discussions = discussions,
                    members = sessionMembers
                )
            }

            clubEntity.toDomain().copy(
                members = members,
                activeSession = activeSession
            )
        } catch (e: Exception) {
            Bark.e("Failed to load club (ID: $clubId) from cache with relationships. Serving incomplete club data.", e)
            // Return just the basic club without relationships if loading fails
            clubEntity.toDomain()
        }
    }

    override suspend fun getClubsForServer(serverId: String): List<Club> {
        return clubDao.getClubsForServer(serverId).map { it.toDomain() }
    }

    override suspend fun insertClub(club: Club) {
        Bark.v("Inserting club (ID: ${club.id}) into database")

        try {
            // One transaction: a club write touches up to six tables, and a half-applied
            // replace would leave the cache worse off than the old add-only behaviour.
            database.withTransaction {
                clubDao.insertClub(club.toEntity())

                // Replace the roster (a null list means "not loaded", so leave it alone).
                club.members?.let { clubMembers ->
                    Bark.v("Caching ${clubMembers.size} members for club (ID: ${club.id})")
                    memberDao.deleteCrossRefsForClub(club.id)
                    clubMembers.forEach { clubMember -> mergeRosterMember(clubMember.member) }
                    memberDao.insertClubMemberCrossRefs(
                        clubMembers.map { clubMember ->
                            ClubMemberCrossRef(
                                clubId = club.id,
                                memberId = clubMember.member.id,
                                role = clubMember.role.name
                            )
                        }
                    )
                }

                // The response is the source of truth for the club's session: drop every
                // cached session that isn't the current active one, along with its children.
                val activeSession = club.activeSession
                sessionDao.getSessionsForClub(club.id)
                    .filter { it.id != activeSession?.id }
                    .forEach { stale ->
                        Bark.v("Removing stale session (ID: ${stale.id}) for club (ID: ${club.id})")
                        discussionDao.deleteDiscussionsForSession(stale.id)
                        sessionDao.deleteSessionMembers(stale.id)
                        sessionDao.deleteSession(stale)
                    }

                activeSession?.let { session ->
                    Bark.v("Caching active session (ID: ${session.id}) for club (ID: ${club.id})")
                    bookDao.insertBook(session.book.toEntity())
                    sessionDao.insertSession(session.toEntity())

                    // Replace the participation list so removed members drop out
                    sessionDao.deleteSessionMembers(session.id)
                    if (session.members.isNotEmpty()) {
                        sessionDao.insertSessionMembers(session.members.map { it.toEntity(session.id) })
                    }

                    // Replace discussions so deleted ones drop out
                    discussionDao.deleteDiscussionsForSession(session.id)
                    if (session.discussions.isNotEmpty()) {
                        discussionDao.insertDiscussions(
                            session.discussions.map { it.toEntity().copy(sessionId = session.id) }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Bark.e("Failed to insert club (ID: ${club.id}) into database. Retry on next sync.", e)
            throw e
        }
    }

    /**
     * Writes a roster member without clobbering a fuller cached record.
     *
     * Club responses embed members without `userId` (see `ClubMemberMappers`), so a plain
     * upsert would wipe the signed-in member's `userId` and break [MemberLocalDataSource.getMemberByUserId].
     * Existing rows keep their `userId`, `createdAt` and `lastFetchedAt`; new roster-only rows get
     * `lastFetchedAt = 0` so they're never mistaken for a fully fetched member.
     */
    private suspend fun mergeRosterMember(member: Member) {
        val rosterEntity = member.toEntity()
        val existing = memberDao.getMember(member.id)
        val merged = if (existing != null) {
            rosterEntity.copy(
                userId = rosterEntity.userId ?: existing.userId,
                createdAt = rosterEntity.createdAt ?: existing.createdAt,
                lastFetchedAt = existing.lastFetchedAt
            )
        } else {
            rosterEntity.copy(lastFetchedAt = 0)
        }
        memberDao.insertMember(merged)
    }

    override suspend fun insertClubs(clubs: List<Club>) {
        Bark.v("Inserting ${clubs.size} clubs into database")
        try {
            clubDao.insertClubs(clubs.map { it.toEntity() })
            Bark.d("Successfully inserted ${clubs.size} clubs into database")
        } catch (e: Exception) {
            Bark.e("Failed to insert ${clubs.size} clubs into database. Retry on next sync.", e)
            throw e
        }
    }

    override suspend fun deleteClub(clubId: String) {
        Bark.v("Deleting club (ID: $clubId) and its relationships from database")
        try {
            database.withTransaction {
                sessionDao.getSessionsForClub(clubId).forEach { session ->
                    discussionDao.deleteDiscussionsForSession(session.id)
                    sessionDao.deleteSessionMembers(session.id)
                }
                sessionDao.deleteSessionsForClub(clubId)
                memberDao.deleteCrossRefsForClub(clubId)
                clubDao.deleteClubById(clubId)
            }
            Bark.d("Successfully deleted club (ID: $clubId) from database")
        } catch (e: Exception) {
            Bark.e("Failed to delete club (ID: $clubId) from database. Retry on next sync.", e)
            throw e
        }
    }

    override suspend fun markStale(clubId: String) {
        Bark.v("Marking club (ID: $clubId) stale")
        clubDao.markStale(clubId)
    }

    override suspend fun getLastFetchedAt(clubId: String): Long? {
        return clubDao.getLastFetchedAt(clubId)
    }

    override suspend fun deleteAll() {
        Bark.v("Clearing all clubs from database")
        try {
            clubDao.deleteAll()
            Bark.d("Successfully cleared all clubs from database")
        } catch (e: Exception) {
            Bark.e("Failed to clear all clubs from database. Retry on next sync.", e)
            throw e
        }
    }
}
