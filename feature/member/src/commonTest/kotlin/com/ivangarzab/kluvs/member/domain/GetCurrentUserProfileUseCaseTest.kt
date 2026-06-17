package com.ivangarzab.kluvs.member.domain

import com.ivangarzab.kluvs.data.repositories.AvatarRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.presentation.util.FormatDateTimeUseCase
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetCurrentUserProfileUseCaseTest {

    private lateinit var memberRepository: MemberRepository
    private lateinit var avatarRepository: AvatarRepository
    private val formatDateTime = FormatDateTimeUseCase()
    private lateinit var useCase: GetCurrentUserProfileUseCase

    @BeforeTest
    fun setup() {
        memberRepository = mock<MemberRepository>()
        avatarRepository = mock<AvatarRepository>()
        useCase = GetCurrentUserProfileUseCase(memberRepository, formatDateTime, avatarRepository)
    }

    @Test
    fun `returns user profile when repository succeeds`() = runTest {
        // Given
        val userId = "user-123"
        val member = Member(
            id = "member-456",
            name = "John Doe",
            userId = userId,
            booksRead = 15
        )
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)
        every { avatarRepository.getAvatarUrl(null) } returns null

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isSuccess)
        val profile = result.getOrNull()!!
        assertEquals("member-456", profile.memberId)
        assertEquals("John Doe", profile.name)
        assertEquals("@johndoe", profile.handle)
        assertEquals("2025", profile.joinDate) // Placeholder until we add created_at
        assertNull(profile.avatarUrl)
        verifySuspend { memberRepository.getMemberByUserId(userId) }
    }

    @Test
    fun `generates handle from name correctly`() = runTest {
        // Given
        val userId = "user-123"
        val member = Member(
            id = "member-456",
            name = "Alice Smith",
            userId = userId,
            booksRead = 0
        )
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)
        every { avatarRepository.getAvatarUrl(null) } returns null


        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("@alicesmith", result.getOrNull()?.handle)
        verifySuspend { memberRepository.getMemberByUserId(userId) }
    }

    @Test
    fun `handles name with multiple spaces`() = runTest {
        // Given
        val userId = "user-123"
        val member = Member(
            id = "member-456",
            name = "Mary Jane Watson",
            userId = userId,
            booksRead = 0
        )
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)
        every { avatarRepository.getAvatarUrl(null) } returns null

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("@maryjanewatson", result.getOrNull()?.handle)
        verifySuspend { memberRepository.getMemberByUserId(userId) }
    }

    @Test
    fun `returns failure when repository fails`() = runTest {
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

    @Test
    fun `populates avatar URL when member has avatar path`() = runTest {
        // Given
        val userId = "user-123"
        val avatarPath = "member-456/avatar.png"
        val avatarUrl = "https://storage.example.com/$avatarPath"
        val member = Member(
            id = "member-456",
            name = "Jane Doe",
            userId = userId,
            avatarPath = avatarPath,
            booksRead = 8
        )
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)
        every { avatarRepository.getAvatarUrl(avatarPath) } returns avatarUrl

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isSuccess)
        val profile = result.getOrNull()!!
        assertEquals(avatarUrl, profile.avatarUrl)
        verify { avatarRepository.getAvatarUrl(avatarPath) }
        verifySuspend { memberRepository.getMemberByUserId(userId) }
    }

    @Test
    fun `avatar URL is null when member has no avatar path`() = runTest {
        // Given
        val userId = "user-123"
        val member = Member(
            id = "member-456",
            name = "Jane Doe",
            userId = userId,
            avatarPath = null,
            booksRead = 8
        )
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)
        every { avatarRepository.getAvatarUrl(null) } returns null

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isSuccess)
        val profile = result.getOrNull()!!
        assertNull(profile.avatarUrl)
        verify { avatarRepository.getAvatarUrl(null) }
        verifySuspend { memberRepository.getMemberByUserId(userId) }
    }
}
