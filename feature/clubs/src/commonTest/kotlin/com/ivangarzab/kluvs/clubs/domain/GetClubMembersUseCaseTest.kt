package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.clubs.domain.GetClubMembersUseCase
import com.ivangarzab.kluvs.data.repositories.AvatarRepository
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.ClubMember
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.model.Role
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GetClubMembersUseCaseTest {

    private lateinit var clubRepository: ClubRepository
    private lateinit var avatarRepository: AvatarRepository
    private lateinit var useCase: GetClubMembersUseCase

    @BeforeTest
    fun setup() {
        clubRepository = mock<ClubRepository>()
        avatarRepository = mock<AvatarRepository>()
        useCase = GetClubMembersUseCase(clubRepository, avatarRepository)
    }

    @Test
    fun `returns members sorted by role`() = runTest {
        // Given: members in mixed order
        val clubId = "club-123"
        val members = listOf(
            ClubMember(
                role = Role.MEMBER,
                member = Member(id = "m1", name = "Alice", userId = null, booksRead = 5)
            ),
            ClubMember(
                role = Role.OWNER,
                member = Member(id = "m2", name = "Bob", userId = null, booksRead = 3)
            ),
            ClubMember(
                role = Role.ADMIN,
                member = Member(id = "m3", name = "Charlie", userId = null, booksRead = 7)
            )
        )
        val club = Club(
            id = clubId,
            name = "Test Club",
            serverId = null,
            discordChannel = null,
            members = members,
            activeSession = null,
            pastSessions = emptyList(),
            shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)
        every { avatarRepository.getAvatarUrl(null) } returns null

        // When
        val result = useCase(clubId)

        // Then: members sorted by role (OWNER, ADMIN, MEMBER)
        assertTrue(result.isSuccess)
        val memberList = result.getOrNull()!!
        assertEquals(3, memberList.size)

        assertEquals("Bob", memberList[0].name)
        assertEquals(Role.OWNER, memberList[0].role)
        assertEquals("Charlie", memberList[1].name)
        assertEquals(Role.ADMIN, memberList[1].role)
        assertEquals("Alice", memberList[2].name)
        assertEquals(Role.MEMBER, memberList[2].role)

        verifySuspend { clubRepository.getClub(clubId) }
    }

    @Test
    fun `returns empty list when club has no members`() = runTest {
        // Given
        val clubId = "club-123"
        val club = Club(
            id = clubId,
            name = "Empty Club",
            serverId = null,
            discordChannel = null,
            members = null,
            activeSession = null,
            pastSessions = emptyList(),
            shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)

        // When
        val result = useCase(clubId)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
        verifySuspend { clubRepository.getClub(clubId) }
    }

    @Test
    fun `returns members with all required fields`() = runTest {
        // Given
        val clubId = "club-123"
        val members = listOf(
            ClubMember(
                role = Role.ADMIN,
                member = Member(id = "m1", name = "Alice", userId = "u1", booksRead = 10)
            )
        )
        val club = Club(
            id = clubId,
            name = "Test Club",
            serverId = null,
            discordChannel = null,
            members = members,
            activeSession = null,
            pastSessions = emptyList(),
            shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)
        every { avatarRepository.getAvatarUrl(null) } returns null

        // When
        val result = useCase(clubId)

        // Then
        assertTrue(result.isSuccess)
        val memberList = result.getOrNull()!!
        assertEquals(1, memberList.size)
        assertEquals("m1", memberList[0].memberId)
        assertEquals("Alice", memberList[0].name)
        assertEquals(null, memberList[0].avatarUrl)
        assertEquals(Role.ADMIN, memberList[0].role)
        assertEquals("u1", memberList[0].userId)
        verifySuspend { clubRepository.getClub(clubId) }
    }

    @Test
    fun `handles members with same role maintains order`() = runTest {
        // Given: multiple members with same role
        val clubId = "club-123"
        val members = listOf(
            ClubMember(
                role = Role.MEMBER,
                member = Member(id = "m1", name = "Alice", userId = null, booksRead = 5)
            ),
            ClubMember(
                role = Role.MEMBER,
                member = Member(id = "m2", name = "Bob", userId = null, booksRead = 3)
            ),
            ClubMember(
                role = Role.MEMBER,
                member = Member(id = "m3", name = "Charlie", userId = null, booksRead = 7)
            )
        )
        val club = Club(
            id = clubId,
            name = "Test Club",
            serverId = null,
            discordChannel = null,
            members = members,
            activeSession = null,
            pastSessions = emptyList(),
            shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)
        every { avatarRepository.getAvatarUrl(null) } returns null

        // When
        val result = useCase(clubId)

        // Then: order maintained for same role
        assertTrue(result.isSuccess)
        val memberList = result.getOrNull()!!
        assertEquals(3, memberList.size)
        assertEquals("Alice", memberList[0].name)
        assertEquals("Bob", memberList[1].name)
        assertEquals("Charlie", memberList[2].name)
        verifySuspend { clubRepository.getClub(clubId) }
    }

    @Test
    fun `returns failure when repository fails`() = runTest {
        // Given
        val clubId = "club-123"
        val exception = Exception("Club not found")
        everySuspend { clubRepository.getClub(clubId) } returns Result.failure(exception)

        // When
        val result = useCase(clubId)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { clubRepository.getClub(clubId) }
    }

    @Test
    fun `populates avatar URLs when members have avatar paths`() = runTest {
        // Given
        val clubId = "club-123"
        val avatarPath1 = "member-1/avatar.png"
        val avatarPath2 = "member-2/avatar.png"
        val avatarUrl1 = "https://storage.example.com/$avatarPath1"
        val avatarUrl2 = "https://storage.example.com/$avatarPath2"
        val members = listOf(
            ClubMember(
                role = Role.MEMBER,
                member = Member(id = "m1", name = "Alice", avatarPath = avatarPath1, booksRead = 10)
            ),
            ClubMember(
                role = Role.MEMBER,
                member = Member(id = "m2", name = "Bob", avatarPath = avatarPath2, booksRead = 5)
            )
        )
        val club = Club(
            id = clubId,
            name = "Test Club",
            serverId = null,
            discordChannel = null,
            members = members,
            activeSession = null,
            pastSessions = emptyList(),
            shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)
        every { avatarRepository.getAvatarUrl(avatarPath1) } returns avatarUrl1
        every { avatarRepository.getAvatarUrl(avatarPath2) } returns avatarUrl2

        // When
        val result = useCase(clubId)

        // Then
        assertTrue(result.isSuccess)
        val memberList = result.getOrNull()!!
        assertEquals(2, memberList.size)
        assertEquals(avatarUrl1, memberList[0].avatarUrl)
        assertEquals(Role.MEMBER, memberList[0].role)
        assertEquals(avatarUrl2, memberList[1].avatarUrl)
        assertEquals(Role.MEMBER, memberList[1].role)
        verify { avatarRepository.getAvatarUrl(avatarPath1) }
        verify { avatarRepository.getAvatarUrl(avatarPath2) }
    }

    @Test
    fun `handles members with mixed avatar presence and sorts by role`() = runTest {
        // Given: MEMBER listed before OWNER
        val clubId = "club-123"
        val avatarPath1 = "member-1/avatar.png"
        val avatarUrl1 = "https://storage.example.com/$avatarPath1"
        val members = listOf(
            ClubMember(role = Role.MEMBER, member = Member(id = "m2", name = "Bob", avatarPath = null, booksRead = 5)),
            ClubMember(role = Role.OWNER, member = Member(id = "m1", name = "Alice", avatarPath = avatarPath1, booksRead = 10))
        )
        val club = Club(
            id = clubId,
            name = "Test Club",
            serverId = null,
            discordChannel = null,
            members = members,
            activeSession = null,
            pastSessions = emptyList(),
            shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)
        every { avatarRepository.getAvatarUrl(avatarPath1) } returns avatarUrl1
        every { avatarRepository.getAvatarUrl(null) } returns null

        // When
        val result = useCase(clubId)

        // Then: OWNER sorted first despite input order
        assertTrue(result.isSuccess)
        val memberList = result.getOrNull()!!
        assertEquals(2, memberList.size)
        assertEquals("Alice", memberList[0].name)
        assertEquals(Role.OWNER, memberList[0].role)
        assertEquals(avatarUrl1, memberList[0].avatarUrl)
        assertEquals("Bob", memberList[1].name)
        assertEquals(Role.MEMBER, memberList[1].role)
        assertEquals(null, memberList[1].avatarUrl)
    }
}
