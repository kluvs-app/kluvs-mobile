package com.ivangarzab.kluvs.member.domain

import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.model.Club
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

class GetUserStatisticsUseCaseTest {

    private lateinit var memberRepository: MemberRepository
    private lateinit var useCase: GetUserStatisticsUseCase

    @BeforeTest
    fun setup() {
        memberRepository = mock<MemberRepository>()
        useCase = GetUserStatisticsUseCase(memberRepository)
    }

    @Test
    fun `returns user statistics when repository succeeds`() = runTest {
        // Given
        val userId = "user-123"
        val clubs = listOf(
            Club(id = "c1", name = "Club 1", serverId = null, discordChannel = null),
            Club(id = "c2", name = "Club 2", serverId = null, discordChannel = null),
            Club(id = "c3", name = "Club 3", serverId = null, discordChannel = null)
        )
        val member = Member(
            id = "member-456",
            name = "John Doe",
            userId = userId,
            booksRead = 20,
            clubs = clubs
        )
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isSuccess)
        val stats = result.getOrNull()!!
        assertEquals(3, stats.clubsCount)
        assertEquals(20, stats.booksRead)
        verifySuspend { memberRepository.getMemberByUserId(userId) }
    }

    @Test
    fun `returns zero clubs count when member has no clubs`() = runTest {
        // Given
        val userId = "user-123"
        val member = Member(
            id = "member-456",
            name = "Jane Doe",
            userId = userId,
            booksRead = 0,
            clubs = null
        )
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isSuccess)
        val stats = result.getOrNull()!!
        assertEquals(0, stats.clubsCount)
        assertEquals(0, stats.booksRead)
        verifySuspend { memberRepository.getMemberByUserId(userId) }
    }

    @Test
    fun `returns zero stats for new member`() = runTest {
        // Given
        val userId = "user-123"
        val member = Member(
            id = "member-456",
            name = "New User",
            userId = userId,
            booksRead = 0,
            clubs = emptyList()
        )
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isSuccess)
        val stats = result.getOrNull()!!
        assertEquals(0, stats.clubsCount)
        assertEquals(0, stats.booksRead)
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
}
