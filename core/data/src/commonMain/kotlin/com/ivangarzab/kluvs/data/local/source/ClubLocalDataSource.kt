package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.kluvs.data.local.mappers.toDomain
import com.ivangarzab.kluvs.data.local.mappers.toEntity
import com.ivangarzab.kluvs.database.KluvsDatabase
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.ClubMember
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
                sessionEntity.toDomain(bookEntity.toDomain()).copy(discussions = discussions)
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
            // Insert club entity
            clubDao.insertClub(club.toEntity())

            // Cache members and relationships
            // Note: Members from Club API response are basic objects without their own relationships.
            // We cache them here so the club can show its member list, but MemberRepository
            // is responsible for caching complete member data.
            club.members?.let { clubMembers ->
                Bark.v("Caching ${clubMembers.size} members for club (ID: ${club.id})")
                memberDao.insertMembers(clubMembers.map { it.member.toEntity() })
                clubMembers.forEach { clubMember ->
                    memberDao.insertClubMemberCrossRef(
                        com.ivangarzab.kluvs.database.entities.ClubMemberCrossRef(
                            clubId = club.id,
                            memberId = clubMember.member.id,
                            role = clubMember.role.name
                        )
                    )
                }
            }

            // Cache active session with book and discussions
            club.activeSession?.let { session ->
                Bark.v("Caching active session (ID: ${session.id}) for club (ID: ${club.id})")

                // Cache the book
                try {
                    bookDao.insertBook(session.book.toEntity())
                } catch (e: Exception) {
                    Bark.e("Failed to cache book (ID: ${session.book.id}) for session (ID: ${session.id}). Retry on next sync.", e)
                }

                // Cache the session
                try {
                    sessionDao.insertSession(session.toEntity())
                } catch (e: Exception) {
                    Bark.e("Failed to cache session (ID: ${session.id}). Retry on next sync.", e)
                }

                // Cache discussions
                session.discussions?.forEach { discussion ->
                    try {
                        discussionDao.insertDiscussion(discussion.toEntity())
                    } catch (e: Exception) {
                        Bark.e("Failed to cache discussion (ID: ${discussion.id}). Retry on next sync.", e)
                    }
                }
            }
        } catch (e: Exception) {
            Bark.e("Failed to insert club (ID: ${club.id}) into database. Retry on next sync.", e)
            throw e
        }
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
        val entity = clubDao.getClub(clubId)
        if (entity != null) {
            Bark.v("Deleting club (ID: $clubId) from database")
            try {
                clubDao.deleteClub(entity)
                Bark.d("Successfully deleted club (ID: $clubId) from database")
            } catch (e: Exception) {
                Bark.e("Failed to delete club (ID: $clubId) from database. Retry on next sync.", e)
                throw e
            }
        }
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
