package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.clubs.domain.GetClubDetailsUseCase
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.ClubMember
import com.ivangarzab.kluvs.model.Discussion
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.model.Role
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

class GetClubDetailsUseCaseTest {

    private lateinit var clubRepository: ClubRepository
    private val formatDateTime = FormatDateTimeUseCase()
    private lateinit var useCase: GetClubDetailsUseCase

    @BeforeTest
    fun setup() {
        clubRepository = mock<ClubRepository>()
        useCase = GetClubDetailsUseCase(clubRepository, formatDateTime)
    }

    @Test
    fun `returns club details when repository succeeds`() = runTest {
        // Given
        val clubId = "club-123"
        val club = Club(
            id = clubId,
            name = "Test Club",
            serverId = null,
            discordChannel = null,
            members = listOf(
                ClubMember(
                    role = Role.MEMBER,
                    member = Member(id = "m1", name = "Alice", userId = null, booksRead = 5)
                ),
                ClubMember(
                    role = Role.MEMBER,
                    member = Member(id = "m2", name = "Bob", userId = null, booksRead = 3)
                )
            ),
            activeSession = null,
            pastSessions = emptyList(),
            shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)

        // When
        val result = useCase(clubId)

        // Then
        assertTrue(result.isSuccess)
        val clubDetails = result.getOrNull()!!
        assertEquals(clubId, clubDetails.clubId)
        assertEquals("Test Club", clubDetails.clubName)
        assertEquals(2, clubDetails.memberCount)
        assertNull(clubDetails.foundedYear)
        assertNull(clubDetails.currentBook)
        assertNull(clubDetails.nextDiscussion)
        verifySuspend { clubRepository.getClub(clubId) }
    }

    @Test
    fun `returns club details with active session and current book`() = runTest {
        // Given
        val clubId = "club-123"
        val book = Book(
            id = "book-1",
            title = "The Hobbit",
            author = "J.R.R. Tolkien",
            edition = null,
            year = 1937,
            isbn = null
        )
        val session = Session(
            id = "session-1",
            clubId = clubId,
            book = book,
            dueDate = LocalDateTime(2026, 3, 15, 0, 0),
            discussions = emptyList()
        )
        val club = Club(
            id = clubId,
            name = "Test Club",
            serverId = null,
            discordChannel = null,
            members = listOf(
                ClubMember(
                    role = Role.MEMBER,
                    member = Member(id = "m1", name = "Alice", userId = null, booksRead = 5)
                )
            ),
            activeSession = session,
            pastSessions = emptyList(),
            shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)

        // When
        val result = useCase(clubId)

        // Then
        assertTrue(result.isSuccess)
        val clubDetails = result.getOrNull()!!
        assertEquals("The Hobbit", clubDetails.currentBook?.title)
        assertEquals("J.R.R. Tolkien", clubDetails.currentBook?.author)
        assertEquals("1937", clubDetails.currentBook?.year)
        assertNull(clubDetails.currentBook?.pageCount)
        verifySuspend { clubRepository.getClub(clubId) }
    }

    @Test
    fun `returns club details with next upcoming discussion`() = runTest {
        // Given
        val clubId = "club-123"
        val futureDate = LocalDateTime(2026, 12, 31, 19, 0)
        val discussion = Discussion(
            id = "disc-1",
            sessionId = "session-1",
            title = "Chapter 1-5",
            date = futureDate,
            location = "Library"
        )
        val session = Session(
            id = "session-1",
            clubId = clubId,
            book = Book(id = "b1", title = "Book", author = "Author", edition = null, year = null, isbn = null),
            dueDate = null,
            discussions = listOf(discussion)
        )
        val club = Club(
            id = clubId,
            name = "Test Club",
            serverId = null,
            discordChannel = null,
            members = emptyList(),
            activeSession = session,
            pastSessions = emptyList(),
            shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)

        // When
        val result = useCase(clubId)

        // Then
        assertTrue(result.isSuccess)
        val clubDetails = result.getOrNull()!!
        assertEquals("Chapter 1-5", clubDetails.nextDiscussion?.title)
        assertEquals("Library", clubDetails.nextDiscussion?.location)
        // Verify formatted date is present and correctly formatted
        assertTrue(clubDetails.nextDiscussion?.formattedDate?.contains("December") == true)
        assertTrue(clubDetails.nextDiscussion?.formattedDate?.contains("2026") == true)
        verifySuspend { clubRepository.getClub(clubId) }
    }

    @Test
    fun `returns zero member count when club has no members`() = runTest {
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
        assertEquals(0, result.getOrNull()?.memberCount)
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
}
