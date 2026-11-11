package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.kluvs.data.DatabaseMockFixture
import com.ivangarzab.kluvs.database.entities.BookEntity
import com.ivangarzab.kluvs.database.entities.ClubEntity
import com.ivangarzab.kluvs.database.entities.MemberEntity
import com.ivangarzab.kluvs.database.entities.SessionEntity
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.model.Session
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matching
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import com.ivangarzab.kluvs.database.entities.DiscussionEntity
import com.ivangarzab.kluvs.model.Discussion
import com.ivangarzab.kluvs.model.ClubMember
import kotlinx.datetime.LocalDateTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ClubLocalDataSourceTest {

    private lateinit var fixture: DatabaseMockFixture
    private lateinit var dataSource: ClubLocalDataSource

    private fun setup() {
        fixture = DatabaseMockFixture()
        dataSource = ClubLocalDataSourceImpl(fixture.database)
    }

    @Test
    fun `getClub returns club when it exists`() = runTest {
        setup()
        val clubId = "club-1"
        val clubEntity = ClubEntity(clubId, null, "Fantasy Club", null, null, 0)

        everySuspend { fixture.clubDao.getClub(clubId) } returns clubEntity
        everySuspend { fixture.memberDao.getMembersForClub(clubId) } returns emptyList()
        everySuspend { fixture.sessionDao.getSessionsForClub(clubId) } returns emptyList()

        val result = dataSource.getClub(clubId)

        assertEquals("Fantasy Club", result?.name)
    }

    @Test
    fun `getClub returns null when club does not exist`() = runTest {
        setup()
        everySuspend { fixture.clubDao.getClub("not-found") } returns null

        assertNull(dataSource.getClub("not-found"))
    }

    @Test
    fun `getClub loads members and activeSession`() = runTest {
        setup()
        val clubId = "club-1"
        val clubEntity = ClubEntity(clubId, null, "Fantasy Club", null, null, 0)
        val memberEntity = MemberEntity("member-1", "user-1", "Alice", "alice", null, 5, null, 0)
        val bookEntity = BookEntity("book-1", "The Hobbit", "Tolkien", null, 1937, null, null, imageUrl = null, externalGoogleId = null, lastFetchedAt = 0)
        val sessionEntity = SessionEntity("session-1", clubId, "book-1", "2026-03-15", 0)

        everySuspend { fixture.clubDao.getClub(clubId) } returns clubEntity
        everySuspend { fixture.memberDao.getMembersForClub(clubId) } returns listOf(memberEntity)
        everySuspend { fixture.memberDao.getClubMemberCrossRefsForClub(clubId) } returns emptyList()
        everySuspend { fixture.sessionDao.getSessionsForClub(clubId) } returns listOf(sessionEntity)
        everySuspend { fixture.bookDao.getBook("book-1") } returns bookEntity
        everySuspend { fixture.discussionDao.getDiscussionsForSession("session-1") } returns emptyList()

        val result = dataSource.getClub(clubId)

        assertEquals("Fantasy Club", result?.name)
        assertEquals(1, result?.members?.size)
        assertEquals("Alice", result?.members?.first()?.member?.name)
        assertEquals("session-1", result?.activeSession?.id)
    }

    @Test
    fun `getClubsForServer returns clubs`() = runTest {
        setup()
        val serverId = "server-1"
        val clubs = listOf(
            ClubEntity("club-1", serverId, "Fantasy Club", null, null, 0),
            ClubEntity("club-2", serverId, "Sci-Fi Club", null, null, 0)
        )

        everySuspend { fixture.clubDao.getClubsForServer(serverId) } returns clubs

        val result = dataSource.getClubsForServer(serverId)

        assertEquals(2, result.size)
        assertEquals("Fantasy Club", result[0].name)
        assertEquals("Sci-Fi Club", result[1].name)
    }

    @Test
    fun `insertClub inserts club with members and session`() = runTest {
        setup()
        val book = Book("book-1", "The Hobbit", "Tolkien", null, 1937, null)
        val session = Session("session-1", "club-1", book, null, emptyList())
        val member = Member("member-1", "user-1", "Alice", "", 5, null)
        val clubMember = com.ivangarzab.kluvs.model.ClubMember(role = Role.MEMBER, member = member)
        val club = Club(
            id = "club-1",
            name = "Fantasy Club",
            discordChannel = null,
            serverId = null,
            foundedDate = null,
            shameList = emptyList(),
            members = listOf(clubMember),
            activeSession = session,
            pastSessions = emptyList()
        )

        everySuspend { fixture.clubDao.insertClub(club.toEntity()) } returns Unit
        everySuspend { fixture.memberDao.insertMembers(listOf(member.toEntity())) } returns Unit
        everySuspend { fixture.memberDao.insertClubMemberCrossRef(any()) } returns Unit
        everySuspend { fixture.bookDao.insertBook(book.toEntity()) } returns Unit
        everySuspend { fixture.sessionDao.insertSession(session.toEntity()) } returns Unit

        dataSource.insertClub(club)
    }

    @Test
    fun `insertClubs inserts multiple clubs`() = runTest {
        setup()
        val clubs = listOf(
            Club("club-1", "Fantasy Club"),
            Club("club-2", "Sci-Fi Club")
        )

        everySuspend { fixture.clubDao.insertClubs(clubs.map { it.toEntity() }) } returns Unit

        dataSource.insertClubs(clubs)
    }

    @Test
    fun `deleteClub deletes existing club`() = runTest {
        setup()
        val entity = ClubEntity("club-1", null, "Fantasy Club", null, null, 0)
        everySuspend { fixture.clubDao.getClub("club-1") } returns entity
        everySuspend { fixture.clubDao.deleteClub(entity) } returns Unit

        dataSource.deleteClub("club-1")
    }

    @Test
    fun `deleteAll clears all clubs`() = runTest {
        setup()

        dataSource.deleteAll()
    }

    private fun Club.toEntity() = ClubEntity(
        id = id,
        serverId = null,
        name = name,
        discordChannel = null,
        foundedDate = null,
        lastFetchedAt = 0
    )

    private fun Member.toEntity() = MemberEntity(
        id = id,
        userId = userId,
        name = name,
        handle = null,
        avatarPath = avatarPath,
        booksRead = booksRead,
        createdAt = null,
        lastFetchedAt = 0
    )

    private fun Book.toEntity() = BookEntity(
        id = id,
        title = title,
        author = author,
        edition = edition,
        year = year,
        isbn = isbn,
        pageCount = null,
        lastFetchedAt = 0,
        imageUrl = imageUrl,
        externalGoogleId = externalGoogleId
    )

    private fun Session.toEntity() = SessionEntity(
        id = id,
        clubId = clubId,
        bookId = book.id,
        dueDate = dueDate?.toString(),
        lastFetchedAt = 0
    )

    // ========================================
    // KT-321: REPLACE SEMANTICS
    // ========================================

    private val hobbit = Book("book-1", "The Hobbit", "Tolkien", null, 1937, null)

    @Test
    fun `insertClub replaces the roster instead of adding to it`() = runTest {
        setup()
        val member = Member(id = "member-1", name = "Alice")
        val club = Club(id = "club-1", name = "Fantasy Club", members = listOf(ClubMember(Role.MEMBER, member)))

        dataSource.insertClub(club)

        verifySuspend(VerifyMode.order) {
            fixture.memberDao.deleteCrossRefsForClub("club-1")
            fixture.memberDao.insertClubMemberCrossRefs(matching { refs -> refs.map { it.memberId } == listOf("member-1") })
        }
    }

    @Test
    fun `insertClub with null members leaves the roster alone`() = runTest {
        setup()

        dataSource.insertClub(Club(id = "club-1", name = "Fantasy Club", members = null))

        verifySuspend(VerifyMode.not) { fixture.memberDao.deleteCrossRefsForClub(any()) }
    }

    @Test
    fun `insertClub without an active session removes the club's cached sessions and their children`() = runTest {
        setup()
        val stale = SessionEntity("session-old", "club-1", "book-1", null, 0)
        everySuspend { fixture.sessionDao.getSessionsForClub("club-1") } returns listOf(stale)

        dataSource.insertClub(Club(id = "club-1", name = "Fantasy Club", members = emptyList(), activeSession = null))

        verifySuspend {
            fixture.discussionDao.deleteDiscussionsForSession("session-old")
            fixture.sessionDao.deleteSessionMembers("session-old")
            fixture.sessionDao.deleteSession(stale)
        }
    }

    @Test
    fun `insertClub keeps the active session and replaces its discussions`() = runTest {
        setup()
        val discussion = Discussion(id = "disc-2", sessionId = "session-1", title = "Ch. 2", date = LocalDateTime.parse("2026-08-01T18:00:00"))
        val session = Session("session-1", "club-1", hobbit, null, listOf(discussion))
        val activeRow = SessionEntity("session-1", "club-1", "book-1", null, 0)
        val oldRow = SessionEntity("session-old", "club-1", "book-1", null, 0)
        everySuspend { fixture.sessionDao.getSessionsForClub("club-1") } returns listOf(activeRow, oldRow)

        dataSource.insertClub(Club(id = "club-1", name = "Fantasy Club", members = emptyList(), activeSession = session))

        verifySuspend(VerifyMode.not) { fixture.sessionDao.deleteSession(activeRow) }
        verifySuspend { fixture.sessionDao.deleteSession(oldRow) }
        verifySuspend(VerifyMode.order) {
            fixture.discussionDao.deleteDiscussionsForSession("session-1")
            fixture.discussionDao.insertDiscussions(matching { rows -> rows.map { it.id } == listOf("disc-2") })
        }
    }

    @Test
    fun `insertClub keeps the cached member's userId and freshness when merging the roster`() = runTest {
        setup()
        val existing = MemberEntity("member-1", "user-1", "Alice", "alice", null, 5, "2025-01-01T00:00", 12345L)
        everySuspend { fixture.memberDao.getMember("member-1") } returns existing
        val rosterMember = Member(id = "member-1", name = "Alice B", userId = null)

        dataSource.insertClub(Club(id = "club-1", name = "Fantasy Club", members = listOf(ClubMember(Role.MEMBER, rosterMember))))

        verifySuspend {
            fixture.memberDao.insertMember(matching {
                it.id == "member-1" && it.userId == "user-1" && it.name == "Alice B" &&
                    it.createdAt == "2025-01-01T00:00" && it.lastFetchedAt == 12345L
            })
        }
    }

    @Test
    fun `insertClub stores a new roster-only member as stale`() = runTest {
        setup()
        val rosterMember = Member(id = "member-9", name = "Zed")

        dataSource.insertClub(Club(id = "club-1", name = "Fantasy Club", members = listOf(ClubMember(Role.MEMBER, rosterMember))))

        verifySuspend { fixture.memberDao.insertMember(matching { it.id == "member-9" && it.lastFetchedAt == 0L }) }
    }

    @Test
    fun `insertClub runs in a single transaction`() = runTest {
        setup()

        dataSource.insertClub(Club(id = "club-1", name = "Fantasy Club", members = emptyList()))

        verifySuspend(VerifyMode.exactly(1)) { fixture.database.withTransaction<Any?>(any()) }
    }

    @Test
    fun `deleteClub removes the club with its links and sessions and their children`() = runTest {
        setup()
        val session = SessionEntity("session-1", "club-1", "book-1", null, 0)
        everySuspend { fixture.sessionDao.getSessionsForClub("club-1") } returns listOf(session)

        dataSource.deleteClub("club-1")

        verifySuspend {
            fixture.discussionDao.deleteDiscussionsForSession("session-1")
            fixture.sessionDao.deleteSessionMembers("session-1")
            fixture.sessionDao.deleteSessionsForClub("club-1")
            fixture.memberDao.deleteCrossRefsForClub("club-1")
            fixture.clubDao.deleteClubById("club-1")
        }
    }

    @Test
    fun `markStale resets the club's freshness`() = runTest {
        setup()

        dataSource.markStale("club-1")

        verifySuspend { fixture.clubDao.markStale("club-1") }
    }
}
