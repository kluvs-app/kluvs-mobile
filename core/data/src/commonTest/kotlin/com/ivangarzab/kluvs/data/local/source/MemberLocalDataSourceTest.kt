package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.kluvs.data.DatabaseMockFixture
import com.ivangarzab.kluvs.database.entities.ClubMemberCrossRef
import com.ivangarzab.kluvs.database.entities.ClubEntity
import com.ivangarzab.kluvs.database.entities.MemberEntity
import com.ivangarzab.kluvs.data.local.mappers.toDomain
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.Member
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MemberLocalDataSourceTest {

    private lateinit var fixture: DatabaseMockFixture
    private lateinit var dataSource: MemberLocalDataSource

    private fun setup() {
        fixture = DatabaseMockFixture()
        dataSource = MemberLocalDataSourceImpl(fixture.database)
    }

    @Test
    fun `getMember returns member with clubs`() = runTest {
        setup()
        val memberId = "member-1"
        val memberEntity = MemberEntity(memberId, "user-1", "Alice", "alice", null, 5, null, 0)
        val clubEntities = listOf(
            ClubEntity("club-1", null, "Fantasy Club", null, null, 0)
        )

        everySuspend { fixture.memberDao.getMember(memberId) } returns memberEntity
        everySuspend { fixture.memberDao.getClubsForMember(memberId) } returns clubEntities
        everySuspend { fixture.memberDao.getClubMemberCrossRefsForMember(memberId) } returns listOf(
            ClubMemberCrossRef("club-1", memberId, "member")
        )

        val result = dataSource.getMember(memberId)

        assertEquals("Alice", result?.name)
        assertEquals(1, result?.clubs?.size)
    }

    @Test
    fun `getMember returns null when member does not exist`() = runTest {
        setup()
        everySuspend { fixture.memberDao.getMember("not-found") } returns null

        assertNull(dataSource.getMember("not-found"))
    }

    @Test
    fun `getMemberByUserId returns member with clubs`() = runTest {
        setup()
        val userId = "user-1"
        val memberId = "member-1"
        val memberEntity = MemberEntity(memberId, userId, "Bob", "bob", null, 2, null, 0)
        val clubEntities = listOf(
            ClubEntity("club-1", null, "Book Club", null, null, 0)
        )

        everySuspend { fixture.memberDao.getMemberByUserId(userId) } returns memberEntity
        everySuspend { fixture.memberDao.getClubsForMember(memberId) } returns clubEntities
        everySuspend { fixture.memberDao.getClubMemberCrossRefsForMember(memberId) } returns listOf(
            ClubMemberCrossRef("club-1", memberId, "owner")
        )

        val result = dataSource.getMemberByUserId(userId)

        assertEquals("Bob", result?.name)
        assertEquals(1, result?.clubs?.size)
    }

    @Test
    fun `getMemberByUserId returns null when user has no member`() = runTest {
        setup()
        everySuspend { fixture.memberDao.getMemberByUserId("user-not-found") } returns null

        assertNull(dataSource.getMemberByUserId("user-not-found"))
    }

    @Test
    fun `getMembersForClub returns members`() = runTest {
        setup()
        val clubId = "club-1"
        val members = listOf(
            MemberEntity("member-1", "user-1", "Alice", "alice", null, 5, null, 0),
            MemberEntity("member-2", "user-2", "Bob", "bob", null, 2, null, 0)
        )

        everySuspend { fixture.memberDao.getMembersForClub(clubId) } returns members

        val result = dataSource.getMembersForClub(clubId)

        assertEquals(2, result.size)
        assertEquals("Alice", result[0].name)
        assertEquals("Bob", result[1].name)
    }

    @Test
    fun `insertMember inserts member and relationships`() = runTest {
        setup()
        val member = Member(
            id = "member-1",
            name = "user-1",
            handle = "Alice",
            avatarPath = "path",
            booksRead = 5,
            clubs = listOf(Club("club-1", "Fantasy Club", null, null, null, emptyList(), null, null, null, emptyList()))
        )

        everySuspend { fixture.memberDao.insertMember(member.toEntity()) } returns Unit
        everySuspend {
            fixture.memberDao.insertClubMemberCrossRef(
                ClubMemberCrossRef("club-1", "member-1", "admin")
            )
        } returns Unit

        dataSource.insertMember(member)
    }

    @Test
    fun `insertMembers inserts multiple members`() = runTest {
        setup()
        val members = listOf(
            Member("member-1", "user-1", "Alice", "", 5, null),
            Member("member-2", "user-2", "Bob", "", 2, null)
        )

        everySuspend {
            fixture.memberDao.insertMembers(members.map { it.toEntity() })
        } returns Unit

        dataSource.insertMembers(members)
    }

    @Test
    fun `insertClubMemberRelationship creates relationship`() = runTest {
        setup()
        everySuspend {
            fixture.memberDao.insertClubMemberCrossRef(
                ClubMemberCrossRef("club-1", "member-1", "member")
            )
        } returns Unit

        dataSource.insertClubMemberRelationship("club-1", "member-1", "member")
    }

    @Test
    fun `deleteClubMemberRelationship removes relationship`() = runTest {
        setup()
        everySuspend {
            fixture.memberDao.deleteClubMemberCrossRef("club-1", "member-1")
        } returns Unit

        dataSource.deleteClubMemberRelationship("club-1", "member-1")
    }

    @Test
    fun `deleteMember deletes existing member`() = runTest {
        setup()
        val entity = MemberEntity("member-1", "user-1", "Alice", "alice", null, 5, null, 0)
        everySuspend { fixture.memberDao.getMember("member-1") } returns entity
        everySuspend { fixture.memberDao.deleteMember(entity) } returns Unit

        dataSource.deleteMember("member-1")
    }

    @Test
    fun `deleteAll clears all members and relationships`() = runTest {
        setup()

        dataSource.deleteAll()
    }

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
}
