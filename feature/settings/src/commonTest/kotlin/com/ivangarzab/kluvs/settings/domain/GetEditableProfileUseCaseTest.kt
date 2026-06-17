package com.ivangarzab.kluvs.settings.domain

import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.model.Member
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetEditableProfileUseCaseTest {

    private lateinit var memberRepository: MemberRepository
    private lateinit var useCase: GetEditableProfileUseCase

    @BeforeTest
    fun setup() {
        memberRepository = mock<MemberRepository>()
        useCase = GetEditableProfileUseCase(memberRepository)
    }

    @Test
    fun `returns editable profile when repository succeeds`() = runTest {
        // Given
        val userId = "user-123"
        val member = Member(
            id = "member-456",
            name = "John Doe",
            handle = "johndoe",
            userId = userId,
            booksRead = 5
        )
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isSuccess)
        val profile = result.getOrNull()!!
        assertEquals("member-456", profile.memberId)
        assertEquals("John Doe", profile.name)
        assertEquals("johndoe", profile.handle)
        verifySuspend { memberRepository.getMemberByUserId(userId) }
    }

    @Test
    fun `derives handle from name when member handle is null`() = runTest {
        // Given
        val userId = "user-123"
        val member = Member(
            id = "member-456",
            name = "Alice Smith",
            handle = null,
            userId = userId,
            booksRead = 0
        )
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("alicesmith", result.getOrNull()?.handle)
        verifySuspend { memberRepository.getMemberByUserId(userId) }
    }

    @Test
    fun `derives handle from multi-word name`() = runTest {
        // Given
        val userId = "user-123"
        val member = Member(
            id = "member-789",
            name = "Mary Jane Watson",
            handle = null,
            userId = userId,
            booksRead = 0
        )
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("maryjanewatson", result.getOrNull()?.handle)
        verifySuspend { memberRepository.getMemberByUserId(userId) }
    }

    @Test
    fun `propagates repository failure`() = runTest {
        // Given
        val userId = "user-123"
        val exception = Exception("Member not found")
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.failure(exception)

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { memberRepository.getMemberByUserId(userId) }
    }
}
