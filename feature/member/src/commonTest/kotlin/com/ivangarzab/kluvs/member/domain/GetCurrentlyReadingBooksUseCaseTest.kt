package com.ivangarzab.kluvs.member.domain

import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.Discussion
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.model.Session
import com.ivangarzab.kluvs.presentation.util.FormatDateTimeUseCase
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetCurrentlyReadingBooksUseCaseTest {

    private lateinit var memberRepository: MemberRepository
    private lateinit var clubRepository: ClubRepository
    private val formatDateTime = FormatDateTimeUseCase()
    private lateinit var useCase: GetCurrentlyReadingBooksUseCase

    @BeforeTest
    fun setup() {
        memberRepository = mock<MemberRepository>()
        clubRepository = mock<ClubRepository>()
        useCase = GetCurrentlyReadingBooksUseCase(memberRepository, clubRepository, formatDateTime)
    }

    @Test
    fun `returns empty list when member has no clubs`() = runTest {
        // Given
        val userId = "user-123"
        val member = Member(
            id = "member-456",
            name = "John Doe",
            userId = userId,
            booksRead = 0,
            clubs = null
        )
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
        verifySuspend { memberRepository.getMemberByUserId(userId) }
    }

    @Test
    fun `returns books from clubs with active sessions`() = runTest {
        // Given
        val userId = "user-123"
        val club1 = Club(id = "c1", name = "Sci-Fi Club", serverId = null, discordChannel = null)
        val club2 = Club(id = "c2", name = "Mystery Club", serverId = null, discordChannel = null)

        val member = Member(
            id = "member-456",
            name = "John Doe",
            userId = userId,
            booksRead = 0,
            clubs = listOf(club1, club2)
        )

        val dueDate1 = LocalDateTime(2025, 3, 15, 0, 0)
        val dueDate2 = LocalDateTime(2025, 4, 1, 0, 0)

        val session1 = Session(
            id = "s1",
            clubId = "c1",
            book = Book(id = "b1", title = "Dune", author = "Frank Herbert", edition = null, year = 1965, isbn = null),
            dueDate = dueDate1,
            discussions = emptyList()
        )

        val session2 = Session(
            id = "s2",
            clubId = "c2",
            book = Book(id = "b2", title = "Sherlock Holmes", author = "Arthur Conan Doyle", edition = null, year = 1892, isbn = null),
            dueDate = dueDate2,
            discussions = emptyList()
        )

        val fullClub1 = club1.copy(activeSession = session1)
        val fullClub2 = club2.copy(activeSession = session2)

        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)
        everySuspend { clubRepository.getClub("c1") } returns Result.success(fullClub1)
        everySuspend { clubRepository.getClub("c2") } returns Result.success(fullClub2)

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isSuccess)
        val books = result.getOrNull()!!
        assertEquals(2, books.size)

        assertEquals("Dune", books[0].bookTitle)
        assertEquals("Sci-Fi Club", books[0].clubName)
        assertTrue(books[0].dueDate!!.contains("March"))
        assertTrue(books[0].dueDate!!.contains("2025"))

        assertEquals("Sherlock Holmes", books[1].bookTitle)
        assertEquals("Mystery Club", books[1].clubName)
        assertTrue(books[1].dueDate!!.contains("April"))
        assertTrue(books[1].dueDate!!.contains("2025"))

        verifySuspend { memberRepository.getMemberByUserId(userId) }
        verifySuspend { clubRepository.getClub("c1") }
        verifySuspend { clubRepository.getClub("c2") }
    }

    @Test
    fun `calculates progress based on completed discussions`() = runTest {
        // Given
        val userId = "user-123"
        val club = Club(id = "c1", name = "Book Club", serverId = null, discordChannel = null)

        val member = Member(
            id = "member-456",
            name = "John Doe",
            userId = userId,
            booksRead = 0,
            clubs = listOf(club)
        )

        val pastDate1 = LocalDateTime(2024, 1, 1, 19, 0)
        val pastDate2 = LocalDateTime(2024, 2, 1, 19, 0)
        val futureDate1 = LocalDateTime(2099, 3, 1, 19, 0)
        val futureDate2 = LocalDateTime(2099, 4, 1, 19, 0)

        val discussions = listOf(
            Discussion(id = "d1", sessionId = "s1", title = "Past 1", date = pastDate1, location = null),
            Discussion(id = "d2", sessionId = "s1", title = "Past 2", date = pastDate2, location = null),
            Discussion(id = "d3", sessionId = "s1", title = "Future 1", date = futureDate1, location = null),
            Discussion(id = "d4", sessionId = "s1", title = "Future 2", date = futureDate2, location = null)
        )

        val session = Session(
            id = "s1",
            clubId = "c1",
            book = Book(id = "b1", title = "Test Book", author = "Test Author", edition = null, year = null, isbn = null),
            dueDate = LocalDateTime(2026, 5, 1, 0, 0),
            discussions = discussions
        )

        val fullClub = club.copy(activeSession = session)

        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)
        everySuspend { clubRepository.getClub("c1") } returns Result.success(fullClub)

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isSuccess)
        val books = result.getOrNull()!!
        assertEquals(1, books.size)

        // 2 past discussions out of 4 total = 50% progress
        assertEquals(0.5f, books[0].progress)

        verifySuspend { memberRepository.getMemberByUserId(userId) }
        verifySuspend { clubRepository.getClub("c1") }
    }

    @Test
    fun `returns zero progress when no discussions exist`() = runTest {
        // Given
        val userId = "user-123"
        val club = Club(id = "c1", name = "Book Club", serverId = null, discordChannel = null)

        val member = Member(
            id = "member-456",
            name = "John Doe",
            userId = userId,
            booksRead = 0,
            clubs = listOf(club)
        )

        val session = Session(
            id = "s1",
            clubId = "c1",
            book = Book(id = "b1", title = "Test Book", author = "Test Author", edition = null, year = null, isbn = null),
            dueDate = null,
            discussions = emptyList()
        )

        val fullClub = club.copy(activeSession = session)

        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)
        everySuspend { clubRepository.getClub("c1") } returns Result.success(fullClub)

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isSuccess)
        val books = result.getOrNull()!!
        assertEquals(1, books.size)
        assertEquals(0.0f, books[0].progress)

        verifySuspend { memberRepository.getMemberByUserId(userId) }
        verifySuspend { clubRepository.getClub("c1") }
    }

    @Test
    fun `skips clubs without active sessions`() = runTest {
        // Given
        val userId = "user-123"
        val club1 = Club(id = "c1", name = "Active Club", serverId = null, discordChannel = null)
        val club2 = Club(id = "c2", name = "Inactive Club", serverId = null, discordChannel = null)

        val member = Member(
            id = "member-456",
            name = "John Doe",
            userId = userId,
            booksRead = 0,
            clubs = listOf(club1, club2)
        )

        val session = Session(
            id = "s1",
            clubId = "c1",
            book = Book(id = "b1", title = "Active Book", author = "Author", edition = null, year = null, isbn = null),
            dueDate = null,
            discussions = emptyList()
        )

        val fullClub1 = club1.copy(activeSession = session)
        val fullClub2 = club2.copy(activeSession = null) // No active session

        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)
        everySuspend { clubRepository.getClub("c1") } returns Result.success(fullClub1)
        everySuspend { clubRepository.getClub("c2") } returns Result.success(fullClub2)

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isSuccess)
        val books = result.getOrNull()!!
        assertEquals(1, books.size) // Only the club with active session
        assertEquals("Active Book", books[0].bookTitle)

        verifySuspend { memberRepository.getMemberByUserId(userId) }
        verifySuspend { clubRepository.getClub("c1") }
        verifySuspend { clubRepository.getClub("c2") }
    }

    @Test
    fun `handles null due date gracefully`() = runTest {
        // Given
        val userId = "user-123"
        val club = Club(id = "c1", name = "Book Club", serverId = null, discordChannel = null)

        val member = Member(
            id = "member-456",
            name = "John Doe",
            userId = userId,
            booksRead = 0,
            clubs = listOf(club)
        )

        val session = Session(
            id = "s1",
            clubId = "c1",
            book = Book(id = "b1", title = "Test Book", author = "Test Author", edition = null, year = null, isbn = null),
            dueDate = null,
            discussions = emptyList()
        )

        val fullClub = club.copy(activeSession = session)

        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)
        everySuspend { clubRepository.getClub("c1") } returns Result.success(fullClub)

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isSuccess)
        val books = result.getOrNull()!!
        assertEquals(1, books.size)
        assertNull(books[0].dueDate)

        verifySuspend { memberRepository.getMemberByUserId(userId) }
        verifySuspend { clubRepository.getClub("c1") }
    }

    @Test
    fun `returns failure when member repository fails`() = runTest {
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
