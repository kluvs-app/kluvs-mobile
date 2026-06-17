package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.clubs.domain.GetActiveSessionUseCase
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.Discussion
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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetActiveSessionUseCaseTest {

    private lateinit var clubRepository: ClubRepository
    private val formatDateTime = FormatDateTimeUseCase()
    private lateinit var useCase: GetActiveSessionUseCase

    @BeforeTest
    fun setup() {
        clubRepository = mock<ClubRepository>()
        useCase = GetActiveSessionUseCase(clubRepository, formatDateTime)
    }

    @Test
    fun `returns null when club has no active session`() = runTest {
        // Given
        val clubId = "club-123"
        val club = Club(
            id = clubId,
            name = "Test Club",
            serverId = null,
            discordChannel = null,
            members = emptyList(),
            activeSession = null,
            pastSessions = emptyList(),
            shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)

        // When
        val result = useCase(clubId)

        // Then
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
        verifySuspend { clubRepository.getClub(clubId) }
    }

    @Test
    fun `returns active session details with book information`() = runTest {
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
        val dueDate = LocalDateTime(2026, 3, 15, 0, 0)
        val session = Session(
            id = "session-1",
            clubId = clubId,
            book = book,
            dueDate = dueDate,
            discussions = emptyList()
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
        val sessionDetails = result.getOrNull()!!
        assertEquals("session-1", sessionDetails.sessionId)
        assertEquals("The Hobbit", sessionDetails.book.title)
        assertEquals("J.R.R. Tolkien", sessionDetails.book.author)
        assertEquals("1937", sessionDetails.book.year)
        // Verify formatted date contains expected parts
        assertTrue(sessionDetails.dueDate.contains("March"))
        assertTrue(sessionDetails.dueDate.contains("2026"))
        verifySuspend { clubRepository.getClub(clubId) }
    }

    @Test
    fun `handles null due date gracefully`() = runTest {
        // Given
        val clubId = "club-123"
        val session = Session(
            id = "session-1",
            clubId = clubId,
            book = Book(id = "b1", title = "Book", author = "Author", edition = null, year = null, isbn = null),
            dueDate = null,
            discussions = emptyList()
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
        assertEquals("No due date", result.getOrNull()?.dueDate)
        verifySuspend { clubRepository.getClub(clubId) }
    }

    @Test
    fun `marks discussions with correct status flags`() = runTest {
        // Given
        val clubId = "club-123"
        val pastDate = LocalDateTime(2024, 1, 1, 19, 0)
        val futureDate1 = LocalDateTime(2099, 6, 1, 19, 0)
        val futureDate2 = LocalDateTime(2099, 7, 1, 19, 0)

        val discussions = listOf(
            Discussion(id = "d1", sessionId = "s1", title = "Past", date = pastDate, location = null),
            Discussion(id = "d2", sessionId = "s1", title = "Next", date = futureDate1, location = null),
            Discussion(id = "d3", sessionId = "s1", title = "Future", date = futureDate2, location = null)
        )

        val session = Session(
            id = "session-1",
            clubId = clubId,
            book = Book(id = "b1", title = "Book", author = "Author", edition = null, year = null, isbn = null),
            dueDate = null,
            discussions = discussions
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
        val timeline = result.getOrNull()!!.discussions
        assertEquals(3, timeline.size)

        // First discussion is past
        assertTrue(timeline[0].isPast)
        assertFalse(timeline[0].isNext)

        // Second discussion is next
        assertFalse(timeline[1].isPast)
        assertTrue(timeline[1].isNext)

        // Third discussion is future
        assertFalse(timeline[2].isPast)
        assertFalse(timeline[2].isNext)

        verifySuspend { clubRepository.getClub(clubId) }
    }

    @Test
    fun `marks all discussions as past when all dates have passed`() = runTest {
        // Given
        val clubId = "club-123"
        val pastDate1 = LocalDateTime(2024, 1, 1, 19, 0)
        val pastDate2 = LocalDateTime(2024, 2, 1, 19, 0)
        val pastDate3 = LocalDateTime(2024, 3, 1, 19, 0)

        val discussions = listOf(
            Discussion(id = "d1", sessionId = "s1", title = "Past 1", date = pastDate1, location = null),
            Discussion(id = "d2", sessionId = "s1", title = "Past 2", date = pastDate2, location = null),
            Discussion(id = "d3", sessionId = "s1", title = "Past 3", date = pastDate3, location = null)
        )

        val session = Session(
            id = "session-1",
            clubId = clubId,
            book = Book(id = "b1", title = "Book", author = "Author", edition = null, year = null, isbn = null),
            dueDate = null,
            discussions = discussions
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
        val timeline = result.getOrNull()!!.discussions
        assertEquals(3, timeline.size)

        // All discussions should be marked as past, none as next
        timeline.forEach { discussion ->
            assertTrue(discussion.isPast, "Discussion '${discussion.title}' should be marked as past")
            assertFalse(discussion.isNext, "Discussion '${discussion.title}' should not be marked as next")
        }

        verifySuspend { clubRepository.getClub(clubId) }
    }

    @Test
    fun `sorts discussions chronologically`() = runTest {
        // Given
        val clubId = "club-123"
        val date1 = LocalDateTime(2026, 3, 1, 19, 0)
        val date2 = LocalDateTime(2026, 1, 15, 19, 0)
        val date3 = LocalDateTime(2026, 2, 1, 19, 0)

        // Deliberately unsorted
        val discussions = listOf(
            Discussion(id = "d1", sessionId = "s1", title = "Third", date = date1, location = null),
            Discussion(id = "d2", sessionId = "s1", title = "First", date = date2, location = null),
            Discussion(id = "d3", sessionId = "s1", title = "Second", date = date3, location = null)
        )

        val session = Session(
            id = "session-1",
            clubId = clubId,
            book = Book(id = "b1", title = "Book", author = "Author", edition = null, year = null, isbn = null),
            dueDate = null,
            discussions = discussions
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
        val timeline = result.getOrNull()!!.discussions
        assertEquals("First", timeline[0].title)
        assertEquals("Second", timeline[1].title)
        assertEquals("Third", timeline[2].title)
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
