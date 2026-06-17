package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.data.local.cache.CachePolicy
import com.ivangarzab.kluvs.data.local.source.MemberLocalDataSource
import com.ivangarzab.kluvs.data.remote.source.MemberRemoteDataSource
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.data.remote.dtos.UpdateMemberRequestDto
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class MemberRepositoryTest {

    private lateinit var remoteDataSource: MemberRemoteDataSource
    private lateinit var localDataSource: MemberLocalDataSource
    private lateinit var cachePolicy: CachePolicy
    private lateinit var repository: MemberRepository

    @BeforeTest
    fun setup() {
        remoteDataSource = mock<MemberRemoteDataSource>()
        localDataSource = mock<MemberLocalDataSource>()
        cachePolicy = CachePolicy()
        repository = MemberRepositoryImpl(remoteDataSource, localDataSource, cachePolicy)

        // Default behavior: cache miss (return null)
        everySuspend { localDataSource.getMember(any()) } returns null
        everySuspend { localDataSource.getMemberByUserId(any()) } returns null
        everySuspend { localDataSource.getLastFetchedAt(any()) } returns null
        everySuspend { localDataSource.insertMember(any()) } returns Unit
        everySuspend { localDataSource.deleteMember(any()) } returns Unit
    }

    // ========================================
    // GET MEMBER
    // ========================================

    @Test
    fun `getMember success returns Member with details`() = runTest {
        val memberId = "member-123"
        val expectedMember = Member(
            id = memberId,
            name = "John Doe",
            booksRead = 5,
            userId = "user-789"
        )
        everySuspend { remoteDataSource.getMember(memberId) } returns Result.success(expectedMember)

        val result = repository.getMember(memberId)

        assertTrue(result.isSuccess)
        assertEquals(expectedMember, result.getOrNull())
        assertEquals("John Doe", result.getOrNull()?.name)
        assertEquals(5, result.getOrNull()?.booksRead)
        verifySuspend { remoteDataSource.getMember(memberId) }
    }

    @Test
    fun `getMember failure returns Result failure`() = runTest {
        val memberId = "member-123"
        val exception = Exception("Member not found")
        everySuspend { remoteDataSource.getMember(memberId) } returns Result.failure(exception)

        val result = repository.getMember(memberId)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { remoteDataSource.getMember(memberId) }
    }

    @Test
    fun `getMember with non-existent member returns failure`() = runTest {
        val memberId = "non-existent"
        val exception = Exception("Member not found")
        everySuspend { remoteDataSource.getMember(memberId) } returns Result.failure(exception)

        val result = repository.getMember(memberId)

        assertTrue(result.isFailure)
        verifySuspend { remoteDataSource.getMember(memberId) }
    }

    // ========================================
    // GET MEMBER BY USER ID
    // ========================================

    @Test
    fun `getMemberByUserId success returns Member`() = runTest {
        val userId = "user-789"
        val expectedMember = Member(
            id = "member-123",
            name = "John",
            booksRead = 3,
            userId = userId
        )
        everySuspend { remoteDataSource.getMemberByUserId(userId) } returns Result.success(expectedMember)

        val result = repository.getMemberByUserId(userId)

        assertTrue(result.isSuccess)
        assertEquals(expectedMember, result.getOrNull())
        assertEquals(userId, result.getOrNull()?.userId)
        verifySuspend { remoteDataSource.getMemberByUserId(userId) }
    }

    @Test
    fun `getMemberByUserId failure returns Result failure`() = runTest {
        val userId = "user-789"
        val exception = Exception("Member not found for user")
        everySuspend { remoteDataSource.getMemberByUserId(userId) } returns Result.failure(exception)

        val result = repository.getMemberByUserId(userId)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { remoteDataSource.getMemberByUserId(userId) }
    }

    // ========================================
    // CREATE MEMBER
    // ========================================

    @Test
    fun `createMember success creates member with all fields`() = runTest {
        val memberName = "Jane Doe"
        val userId = "user-456"
        val role = "Admin"
        val clubIds = listOf("club-1", "club-2")
        val expectedMember = Member(
            id = "member-new",
            name = memberName,
            userId = userId,
            booksRead = 0
        )
        everySuspend { remoteDataSource.createMember(any()) } returns Result.success(expectedMember)

        val result = repository.createMember(memberName, userId, role, clubIds)

        assertTrue(result.isSuccess)
        assertEquals(expectedMember, result.getOrNull())
        assertEquals(memberName, result.getOrNull()?.name)
        verifySuspend { remoteDataSource.createMember(any()) }
    }

    @Test
    fun `createMember success creates member without optional fields`() = runTest {
        val memberName = "Simple Member"
        val expectedMember = Member(
            id = "member-new",
            name = memberName,
            booksRead = 0
        )
        everySuspend { remoteDataSource.createMember(any()) } returns Result.success(expectedMember)

        val result = repository.createMember(memberName, null, null, null)

        assertTrue(result.isSuccess)
        assertEquals(expectedMember, result.getOrNull())
        assertEquals(memberName, result.getOrNull()?.name)
        verifySuspend { remoteDataSource.createMember(any()) }
    }

    @Test
    fun `createMember using default clubIds parameter`() = runTest {
        val memberName = "Member Without Clubs"
        val expectedMember = Member(
            id = "member-no-clubs",
            name = memberName,
            booksRead = 0
        )
        everySuspend { remoteDataSource.createMember(any()) } returns Result.success(expectedMember)

        val result = repository.createMember(memberName, "user-123", "Reader")

        assertTrue(result.isSuccess)
        assertEquals(expectedMember, result.getOrNull())
        verifySuspend { remoteDataSource.createMember(any()) }
    }

    @Test
    fun `createMember with club IDs adds member to clubs`() = runTest {
        val memberName = "New Member"
        val clubIds = listOf("club-1", "club-2", "club-3")
        val expectedMember = Member(
            id = "member-new",
            name = memberName,
            booksRead = 0
        )
        everySuspend { remoteDataSource.createMember(any()) } returns Result.success(expectedMember)

        val result = repository.createMember(memberName, null, null, clubIds)

        assertTrue(result.isSuccess)
        verifySuspend { remoteDataSource.createMember(any()) }
    }

    @Test
    fun `createMember failure returns Result failure`() = runTest {
        val exception = Exception("Failed to create member")
        everySuspend { remoteDataSource.createMember(any()) } returns Result.failure(exception)

        val result = repository.createMember("Jane", null, null, null)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { remoteDataSource.createMember(any()) }
    }

    // ========================================
    // UPDATE MEMBER
    // ========================================

    @Test
    fun `updateMember with all fields updates all values`() = runTest {
        val memberId = "member-123"
        val newName = "Updated Name"
        val newUserId = "user-new"
        val newRole = "Moderator"
        val newBooksRead = 15
        val expectedMember = Member(
            id = memberId,
            name = newName,
            userId = newUserId,
            booksRead = newBooksRead
        )
        everySuspend { remoteDataSource.updateMember(any()) } returns Result.success(expectedMember)

        val result = repository.updateMember(
            memberId = memberId,
            name = newName,
            userId = newUserId,
            role = newRole,
            booksRead = newBooksRead
        )

        assertTrue(result.isSuccess)
        assertEquals(newName, result.getOrNull()?.name)
        assertEquals(newBooksRead, result.getOrNull()?.booksRead)
        verifySuspend { remoteDataSource.updateMember(any()) }
    }

    @Test
    fun `updateMember with null name does not update name`() = runTest {
        val memberId = "member-123"
        val expectedMember = Member(
            id = memberId,
            name = "Unchanged",
            booksRead = 10
        )
        everySuspend { remoteDataSource.updateMember(any()) } returns Result.success(expectedMember)

        val result = repository.updateMember(memberId = memberId, name = null)

        assertTrue(result.isSuccess)
        assertEquals("Unchanged", result.getOrNull()?.name)
        verifySuspend { remoteDataSource.updateMember(any()) }
    }

    @Test
    fun `updateMember with clubIds replaces all club memberships`() = runTest {
        val memberId = "member-123"
        val newClubIds = listOf("club-5", "club-6")
        val expectedMember = Member(
            id = memberId,
            name = "Member",
            booksRead = 5
        )
        everySuspend { remoteDataSource.updateMember(any()) } returns Result.success(expectedMember)

        val result = repository.updateMember(memberId = memberId, clubIds = newClubIds)

        assertTrue(result.isSuccess)
        verifySuspend { remoteDataSource.updateMember(any()) }
    }

    @Test
    fun `updateMember with empty clubIds removes member from all clubs`() = runTest {
        val memberId = "member-123"
        val emptyClubIds = emptyList<String>()
        val expectedMember = Member(
            id = memberId,
            name = "Member",
            booksRead = 5
        )
        everySuspend { remoteDataSource.updateMember(any()) } returns Result.success(expectedMember)

        val result = repository.updateMember(memberId = memberId, clubIds = emptyClubIds)

        assertTrue(result.isSuccess)
        verifySuspend { remoteDataSource.updateMember(any()) }
    }

    @Test
    fun `updateMember with null clubIds does not change club memberships`() = runTest {
        val memberId = "member-123"
        val expectedMember = Member(
            id = memberId,
            name = "Updated Name",
            booksRead = 5
        )
        everySuspend { remoteDataSource.updateMember(any()) } returns Result.success(expectedMember)

        val result = repository.updateMember(memberId = memberId, name = "Updated Name", clubIds = null)

        assertTrue(result.isSuccess)
        assertEquals("Updated Name", result.getOrNull()?.name)
        verifySuspend { remoteDataSource.updateMember(any()) }
    }

    @Test
    fun `updateMember with handle updates handle`() = runTest {
        val memberId = "member-123"
        val newHandle = "alice_reads"
        val expectedMember = Member(
            id = memberId,
            name = "Alice",
            handle = newHandle,
            booksRead = 5
        )
        everySuspend { remoteDataSource.updateMember(any()) } returns Result.success(expectedMember)

        val result = repository.updateMember(memberId = memberId, handle = newHandle)

        assertTrue(result.isSuccess)
        assertEquals(newHandle, result.getOrNull()?.handle)
        verifySuspend { remoteDataSource.updateMember(any()) }
    }

    @Test
    fun `updateMember with clubRoles passes club_roles to remote data source`() = runTest {
        val memberId = "member-123"
        val clubRoles = mapOf("club-1" to "admin")
        val expectedMember = Member(id = memberId, name = "Alice", booksRead = 5)
        everySuspend { remoteDataSource.updateMember(any()) } returns Result.success(expectedMember)

        val result = repository.updateMember(memberId = memberId, clubRoles = clubRoles)

        assertTrue(result.isSuccess)
        verifySuspend {
            remoteDataSource.updateMember(
                UpdateMemberRequestDto(id = memberId, club_roles = clubRoles)
            )
        }
    }

    @Test
    fun `updateMember failure returns Result failure`() = runTest {
        val exception = Exception("Failed to update member")
        everySuspend { remoteDataSource.updateMember(any()) } returns Result.failure(exception)

        val result = repository.updateMember("member-123", name = "Updated")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { remoteDataSource.updateMember(any()) }
    }

    // ========================================
    // DELETE MEMBER
    // ========================================

    @Test
    fun `deleteMember success returns success message`() = runTest {
        val memberId = "member-123"
        val successMessage = "Member deleted successfully"
        everySuspend { remoteDataSource.deleteMember(memberId) } returns Result.success(successMessage)

        val result = repository.deleteMember(memberId)

        assertTrue(result.isSuccess)
        assertEquals(successMessage, result.getOrNull())
        verifySuspend { remoteDataSource.deleteMember(memberId) }
    }

    @Test
    fun `deleteMember failure returns Result failure`() = runTest {
        val memberId = "member-123"
        val exception = Exception("Failed to delete member")
        everySuspend { remoteDataSource.deleteMember(memberId) } returns Result.failure(exception)

        val result = repository.deleteMember(memberId)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { remoteDataSource.deleteMember(memberId) }
    }

    @Test
    fun `deleteMember with non-existent member returns failure`() = runTest {
        val memberId = "non-existent"
        val exception = Exception("Member not found")
        everySuspend { remoteDataSource.deleteMember(memberId) } returns Result.failure(exception)

        val result = repository.deleteMember(memberId)

        assertTrue(result.isFailure)
        verifySuspend { remoteDataSource.deleteMember(memberId) }
    }
}
