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
        val updatedMember = Member(id = "member-1", name = "Alice", handle = "alice-reads", booksRead = 5)
        everySuspend { memberRepository.updateMember(any(), any(), any(), any(), any(), any(), any()) } returns Result.success(updatedMember)

        // When
        val result = useCase("member-1", "Alice", "alice-reads")

        // Then
        assertTrue(result.isSuccess)
        verifySuspend { memberRepository.updateMember(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `handle is saved without the at-sign prefix`() = runTest {
        // Given — the backend's own convention (see kluvs-backend's seed.sql) stores the bare
        // handle; every display site prepends its own "@", so saving it pre-prefixed here used
        // to double up as "@@handle" wherever it was shown.
        val updatedMember = Member(id = "member-1", name = "Alice", handle = "alice-reads", booksRead = 5)
        everySuspend { memberRepository.updateMember(any(), any(), any(), any(), any(), any(), any()) } returns Result.success(updatedMember)

        // When
        useCase("member-1", "Alice", "alice-reads")

        // Then
        verifySuspend {
            memberRepository.updateMember(memberId = "member-1", name = "Alice", handle = "alice-reads", any(), any(), any(), any())
        }
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
    fun `handle with underscores returns validation error`() = runTest {
        // When — backend now requires lowercase letters, digits, and hyphens only.
        val result = useCase("member-1", "Alice", "alice_reads_2025")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `handle with uppercase returns validation error`() = runTest {
        // When — case normalization happens in the UI layer (SettingsViewModel), not here; the
        // UseCase enforces the backend's exact format.
        val result = useCase("member-1", "Alice", "AliceReads")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `handle with hyphenated segments is valid`() = runTest {
        // Given
        val updatedMember = Member(id = "member-1", name = "Alice", handle = "alice-reads-2025", booksRead = 5)
        everySuspend { memberRepository.updateMember(any(), any(), any(), any(), any(), any(), any()) } returns Result.success(updatedMember)

        // When
        val result = useCase("member-1", "Alice", "alice-reads-2025")

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `handle with leading hyphen returns validation error`() = runTest {
        // When
        val result = useCase("member-1", "Alice", "-alicereads")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `propagates repository failure`() = runTest {
        // Given
        val exception = Exception("Network error")
        everySuspend { memberRepository.updateMember(any(), any(), any(), any(), any(), any(), any()) } returns Result.failure(exception)

        // When
        val result = useCase("member-1", "Alice", "alice-reads")

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
