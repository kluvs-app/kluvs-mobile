package com.ivangarzab.kluvs.settings.domain

import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.model.Member
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

class UpdateUserProfileUseCaseTest {

    private lateinit var memberRepository: MemberRepository
    private lateinit var useCase: UpdateUserProfileUseCase

    @BeforeTest
    fun setup() {
        memberRepository = mock<MemberRepository>()
        useCase = UpdateUserProfileUseCase(memberRepository)
    }

    @Test
    fun `successful update returns success`() = runTest {
        // Given
        val updatedMember = Member(id = "member-1", name = "Alice", handle = "alice_reads", booksRead = 5)
        everySuspend { memberRepository.updateMember(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.success(updatedMember)

        // When
        val result = useCase("member-1", "Alice", "alice_reads")

        // Then
        assertTrue(result.isSuccess)
        verifySuspend { memberRepository.updateMember(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `blank name returns validation error`() = runTest {
        // When
        val result = useCase("member-1", "  ", "alice_reads")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Name"))
    }

    @Test
    fun `blank handle returns validation error`() = runTest {
        // When
        val result = useCase("member-1", "Alice", "  ")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Handle"))
    }

    @Test
    fun `handle with spaces returns validation error`() = runTest {
        // When
        val result = useCase("member-1", "Alice", "alice reads")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `handle with special characters returns validation error`() = runTest {
        // When
        val result = useCase("member-1", "Alice", "alice@reads!")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `handle shorter than 2 characters returns validation error`() = runTest {
        // When
        val result = useCase("member-1", "Alice", "a")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `handle with underscores is valid`() = runTest {
        // Given
        val updatedMember = Member(id = "member-1", name = "Alice", handle = "alice_reads_2025", booksRead = 5)
        everySuspend { memberRepository.updateMember(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.success(updatedMember)

        // When
        val result = useCase("member-1", "Alice", "alice_reads_2025")

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `propagates repository failure`() = runTest {
        // Given
        val exception = Exception("Network error")
        everySuspend { memberRepository.updateMember(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.failure(exception)

        // When
        val result = useCase("member-1", "Alice", "alice_reads")

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
