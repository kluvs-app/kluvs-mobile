package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.kluvs.data.remote.api.MemberService
import com.ivangarzab.kluvs.data.remote.dtos.ClubDto
import com.ivangarzab.kluvs.data.remote.dtos.CreateMemberRequestDto
import com.ivangarzab.kluvs.data.remote.dtos.DeleteResponseDto
import com.ivangarzab.kluvs.data.remote.dtos.MemberDto
import com.ivangarzab.kluvs.data.remote.dtos.MemberResponseDto
import com.ivangarzab.kluvs.data.remote.dtos.MemberSuccessResponseDto
import com.ivangarzab.kluvs.data.remote.dtos.UpdateMemberRequestDto
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MemberRemoteDataSourceTest {

    private lateinit var memberService: MemberService
    private lateinit var dataSource: MemberRemoteDataSource

    @BeforeTest
    fun setup() {
        memberService = mock<MemberService>()
        dataSource = MemberRemoteDataSourceImpl(memberService)
    }

    @Test
    fun `getMember success returns mapped Member domain model`() = runTest {
        // Given: Service returns MemberResponseDto
        val dto = MemberResponseDto(
            id = "1",
            name = "Alice",
            books_read = 12,
            user_id = "user-123",
            clubs = listOf(
                ClubDto("club-1", "Fiction Club", "123456789", "server-1")
            ),
            shame_clubs = emptyList()
        )

        everySuspend { memberService.get("1") } returns dto

        // When: Getting member
        val result = dataSource.getMember("1")

        // Then: Result is success with mapped domain model
        assertTrue(result.isSuccess)
        val member = result.getOrNull()!!
        assertEquals("1", member.id)
        assertEquals("Alice", member.name)
        assertEquals(12, member.booksRead)
        assertEquals(1, member.clubs?.size)
        assertEquals("Fiction Club", member.clubs?.first()?.name)

        verifySuspend { memberService.get("1") }
    }

    @Test
    fun `getMember failure returns Result failure`() = runTest {
        // Given: Service throws exception
        val exception = Exception("Member not found")
        everySuspend { memberService.get("999") } throws exception

        // When: Getting member
        val result = dataSource.getMember("999")

        // Then: Result is failure
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())

        verifySuspend { memberService.get("999") }
    }

    @Test
    fun `getMemberByUserId success returns mapped Member`() = runTest {
        // Given: Service returns member by user ID
        val dto = MemberResponseDto(
            id = "2",
            name = "Bob",
            books_read = 5,
            user_id = "user-456",
            clubs = emptyList(),
            shame_clubs = emptyList()
        )

        everySuspend { memberService.getByUserId("user-456") } returns dto

        // When: Getting member by user ID
        val result = dataSource.getMemberByUserId("user-456")

        // Then: Result is success
        assertTrue(result.isSuccess)
        assertEquals("2", result.getOrNull()?.id)
        assertEquals("user-456", result.getOrNull()?.userId)

        verifySuspend { memberService.getByUserId("user-456") }
    }

    @Test
    fun `createMember success returns created Member`() = runTest {
        // Given: Service returns success response
        val request = CreateMemberRequestDto(
            name = "Charlie",
            books_read = 0,
            user_id = "user-789"
        )

        val responseDto = MemberSuccessResponseDto(
            success = true,
            message = "Created",
            member = MemberDto(
                id = "3",
                name = "Charlie",
                books_read = 0,
                user_id = "user-789",
                clubs = emptyList()
            )
        )

        everySuspend { memberService.create(request) } returns responseDto

        // When: Creating member
        val result = dataSource.createMember(request)

        // Then: Result is success
        assertTrue(result.isSuccess)
        val member = result.getOrNull()!!
        assertEquals("3", member.id)
        assertEquals("Charlie", member.name)

        verifySuspend { memberService.create(request) }
    }

    @Test
    fun `updateMember success returns updated Member`() = runTest {
        // Given: Service returns success response
        val request = UpdateMemberRequestDto(
            id = "1",
            name = "Alice Updated"
        )

        val responseDto = MemberSuccessResponseDto(
            success = true,
            message = "Updated",
            member = MemberDto(
                id = "1",
                name = "Alice Updated",
                books_read = 12,
                user_id = "user-123",
                clubs = emptyList()
            )
        )

        everySuspend { memberService.update(request) } returns responseDto

        // When: Updating member
        val result = dataSource.updateMember(request)

        // Then: Result is success
        assertTrue(result.isSuccess)
        assertEquals("Alice Updated", result.getOrNull()?.name)

        verifySuspend { memberService.update(request) }
    }

    @Test
    fun `updateMember with handle passes handle in DTO`() = runTest {
        // Given: Request includes a handle
        val request = UpdateMemberRequestDto(
            id = "1",
            name = "Alice",
            handle = "alice_reads"
        )

        val responseDto = MemberSuccessResponseDto(
            success = true,
            message = "Updated",
            member = MemberDto(
                id = "1",
                name = "Alice",
                handle = "alice_reads",
                books_read = 12,
                user_id = "user-123",
                clubs = emptyList()
            )
        )

        everySuspend { memberService.update(request) } returns responseDto

        // When: Updating member
        val result = dataSource.updateMember(request)

        // Then: Result is success and handle is returned
        assertTrue(result.isSuccess)
        assertEquals("alice_reads", result.getOrNull()?.handle)

        verifySuspend { memberService.update(request) }
    }

    @Test
    fun `deleteMember success returns success message`() = runTest {
        // Given: Service returns success response
        val response = DeleteResponseDto(
            success = true,
            message = "Member deleted"
        )

        everySuspend { memberService.delete("1") } returns response

        // When: Deleting member
        val result = dataSource.deleteMember("1")

        // Then: Result is success
        assertTrue(result.isSuccess)
        assertEquals("Member deleted", result.getOrNull())

        verifySuspend { memberService.delete("1") }
    }

    @Test
    fun `deleteMember with success false returns failure`() = runTest {
        // Given: Service returns failure response
        val response = DeleteResponseDto(
            success = false,
            message = "Cannot delete member"
        )

        everySuspend { memberService.delete("1") } returns response

        // When: Deleting member
        val result = dataSource.deleteMember("1")

        // Then: Result is failure
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Cannot delete member") == true)

        verifySuspend { memberService.delete("1") }
    }
}
