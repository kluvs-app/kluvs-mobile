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
}
