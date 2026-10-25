package com.ivangarzab.kluvs.settings.domain

import com.ivangarzab.kluvs.auth.domain.AuthError
import com.ivangarzab.kluvs.auth.domain.AuthRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeleteAccountUseCaseTest {

    private lateinit var memberRepository: MemberRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: DeleteAccountUseCase

    private val memberId = "member-1"
    private val deleteError = Exception("Request timed out")

    @BeforeTest
    fun setup() {
        memberRepository = mock<MemberRepository>()
        authRepository = mock<AuthRepository>()
        useCase = DeleteAccountUseCase(memberRepository, authRepository)
    }

    @Test
    fun `successful delete returns success without checking the account`() = runTest {
        // Given
        everySuspend { memberRepository.deleteMember(memberId) } returns Result.success("Account deleted")

        // When
        val result = useCase(memberId)

        // Then
        assertTrue(result.isSuccess)
        verifySuspend(mode = VerifyMode.not) { authRepository.currentUserExists() }
    }

    @Test
    fun `failed delete returns failure when the account still exists`() = runTest {
        // Given
        everySuspend { memberRepository.deleteMember(memberId) } returns Result.failure(deleteError)
        everySuspend { authRepository.currentUserExists() } returns Result.success(true)

        // When
        val result = useCase(memberId)

        // Then
        assertTrue(result.isFailure)
        assertEquals(deleteError, result.exceptionOrNull())
    }

    @Test
    fun `failed delete returns success when the account no longer exists`() = runTest {
        // Given: the backend deleted the account, but the response never made it back
        everySuspend { memberRepository.deleteMember(memberId) } returns Result.failure(deleteError)
        everySuspend { authRepository.currentUserExists() } returns Result.success(false)

        // When
        val result = useCase(memberId)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `failed delete returns the original failure when the account check also fails`() = runTest {
        // Given
        everySuspend { memberRepository.deleteMember(memberId) } returns Result.failure(deleteError)
        everySuspend { authRepository.currentUserExists() } returns Result.failure(AuthError.NoConnection)

        // When
        val result = useCase(memberId)

        // Then
        assertTrue(result.isFailure)
        assertEquals(deleteError, result.exceptionOrNull())
    }
}
