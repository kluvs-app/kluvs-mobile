package com.ivangarzab.kluvs.member.domain

import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.data.repositories.ProgressRepository
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.Discussion
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.model.ProgressStatus
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.ReadingProgress
import com.ivangarzab.kluvs.model.Session
import com.ivangarzab.kluvs.presentation.progress.GetSessionProgressUseCase
import com.ivangarzab.kluvs.presentation.util.FormatDateTimeUseCase
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetOnYourShelfUseCaseTest {

    private lateinit var memberRepository: MemberRepository
    private lateinit var clubRepository: ClubRepository
    private lateinit var progressRepository: ProgressRepository
    private val formatDateTime = FormatDateTimeUseCase()
    private lateinit var useCase: GetOnYourShelfUseCase

    @BeforeTest
    fun setup() {
        memberRepository = mock<MemberRepository>()
        clubRepository = mock<ClubRepository>()
        progressRepository = mock<ProgressRepository>()
        everySuspend { progressRepository.getProgress(any(), any(), any()) } returns Result.success(emptyList())
        useCase = GetOnYourShelfUseCase(memberRepository, clubRepository, GetSessionProgressUseCase(progressRepository), formatDateTime)
    }

    @Test
    fun `returns empty list when member has no clubs`() = runTest {
        val userId = "user-123"
        val member = Member(id = "member-456", name = "John Doe", userId = userId, booksRead = 0, clubs = null)
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)

        val result = useCase(userId)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.shelf.isEmpty())
        assertNull(result.getOrNull()!!.upNext)
        verifySuspend { memberRepository.getMemberByUserId(userId) }
    }

    @Test
    fun `returns shelf items from clubs with active sessions`() = runTest {
        val userId = "user-123"
        val club1 = Club(id = "c1", name = "Sci-Fi Club", serverId = null, discordChannel = null)
        val club2 = Club(id = "c2", name = "Mystery Club", serverId = null, discordChannel = null)

        val member = Member(id = "member-456", name = "John Doe", userId = userId, booksRead = 0, clubs = listOf(club1, club2))

        val session1 = Session(
            id = "s1",
            clubId = "c1",
            book = Book(id = "b1", title = "Dune", author = "Frank Herbert", edition = null, year = 1965, isbn = null, pageCount = 412, imageUrl = "dune.jpg"),
            dueDate = LocalDateTime(2025, 3, 15, 0, 0),
            discussions = emptyList()
        )
        val session2 = Session(
            id = "s2",
            clubId = "c2",
            book = Book(id = "b2", title = "Sherlock Holmes", author = "Arthur Conan Doyle", edition = null, year = 1892, isbn = null),
            dueDate = LocalDateTime(2025, 4, 1, 0, 0),
            discussions = emptyList()
        )

        val fullClub1 = club1.copy(activeSession = session1)
        val fullClub2 = club2.copy(activeSession = session2)

        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)
        everySuspend { clubRepository.getClub("c1") } returns Result.success(fullClub1)
        everySuspend { clubRepository.getClub("c2") } returns Result.success(fullClub2)

        val result = useCase(userId)

        assertTrue(result.isSuccess)
        val items = result.getOrNull()!!.shelf
        assertEquals(2, items.size)

        assertEquals("Dune", items[0].bookTitle)
        assertEquals("b1", items[0].bookId)
        assertEquals("dune.jpg", items[0].bookCoverUrl)
        assertEquals(412, items[0].bookPageCount)
        assertEquals("Sci-Fi Club", items[0].clubName)
        assertEquals("Sherlock Holmes", items[1].bookTitle)
        assertEquals("Mystery Club", items[1].clubName)

        verifySuspend { memberRepository.getMemberByUserId(userId) }
        verifySuspend { clubRepository.getClub("c1") }
        verifySuspend { clubRepository.getClub("c2") }
    }

    @Test
    fun `includes own progress from GetSessionProgressUseCase`() = runTest {
        val userId = "user-123"
        val club = Club(id = "c1", name = "Book Club", serverId = null, discordChannel = null)
        val member = Member(id = "member-456", name = "John Doe", userId = userId, booksRead = 0, clubs = listOf(club))
        val session = Session(
            id = "s1",
            clubId = "c1",
            book = Book(id = "b1", title = "Test Book", author = "Test Author", edition = null, year = null, isbn = null, pageCount = 200),
            dueDate = null,
            discussions = emptyList()
        )
        val fullClub = club.copy(activeSession = session)
        val progress = ReadingProgress(
            id = "p1",
            memberId = "7",
            bookId = "b1",
            sessionId = "s1",
            type = ProgressType.PAGE,
            status = ProgressStatus.IN_PROGRESS,
            currentPage = 50
        )

        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)
        everySuspend { clubRepository.getClub("c1") } returns Result.success(fullClub)
        everySuspend { progressRepository.getProgress(any(), any(), any()) } returns Result.success(listOf(progress))

        val result = useCase(userId)

        val ownProgress = result.getOrNull()!!.shelf[0].ownProgress
        assertEquals("p1", ownProgress?.progressId)
        assertEquals("50 of 200 pages", ownProgress?.label)
        verifySuspend { progressRepository.getProgress(any(), any(), any()) }
    }

    @Test
    fun `computes next discussion date from earliest future discussion`() = runTest {
        val userId = "user-123"
        val club = Club(id = "c1", name = "Book Club", serverId = null, discordChannel = null)
        val member = Member(id = "member-456", name = "John Doe", userId = userId, booksRead = 0, clubs = listOf(club))

        val pastDate = LocalDateTime(2024, 1, 1, 19, 0)
        val futureDate1 = LocalDateTime(2099, 3, 1, 19, 0)
        val futureDate2 = LocalDateTime(2099, 4, 1, 19, 0)
        val discussions = listOf(
            Discussion(id = "d1", sessionId = "s1", title = "Past", date = pastDate, location = null),
            Discussion(id = "d2", sessionId = "s1", title = "Future 1", date = futureDate1, location = null),
            Discussion(id = "d3", sessionId = "s1", title = "Future 2", date = futureDate2, location = null)
        )
        val session = Session(
            id = "s1",
            clubId = "c1",
            book = Book(id = "b1", title = "Test Book", author = "Test Author", edition = null, year = null, isbn = null),
            dueDate = null,
            discussions = discussions
        )
        val fullClub = club.copy(activeSession = session)

        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)
        everySuspend { clubRepository.getClub("c1") } returns Result.success(fullClub)

        val result = useCase(userId)

        val nextDate = result.getOrNull()!!.shelf[0].nextDiscussionDate
        assertTrue(nextDate!!.contains("March"))
        assertTrue(nextDate.contains("2099"))

        val upNext = result.getOrNull()!!.upNext
        assertEquals("Future 1", upNext?.title)
        assertEquals("Book Club", upNext?.clubName)
        assertTrue(upNext?.date?.contains("March") == true)
    }

    @Test
    fun `returns null next discussion date when no discussions exist`() = runTest {
        val userId = "user-123"
        val club = Club(id = "c1", name = "Book Club", serverId = null, discordChannel = null)
        val member = Member(id = "member-456", name = "John Doe", userId = userId, booksRead = 0, clubs = listOf(club))
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

        val result = useCase(userId)

        assertNull(result.getOrNull()!!.shelf[0].nextDiscussionDate)
        assertNull(result.getOrNull()!!.upNext)
    }

    @Test
    fun `skips clubs without active sessions`() = runTest {
        val userId = "user-123"
        val club1 = Club(id = "c1", name = "Active Club", serverId = null, discordChannel = null)
        val club2 = Club(id = "c2", name = "Inactive Club", serverId = null, discordChannel = null)
        val member = Member(id = "member-456", name = "John Doe", userId = userId, booksRead = 0, clubs = listOf(club1, club2))
        val session = Session(
            id = "s1",
            clubId = "c1",
            book = Book(id = "b1", title = "Active Book", author = "Author", edition = null, year = null, isbn = null),
            dueDate = null,
            discussions = emptyList()
        )
        val fullClub1 = club1.copy(activeSession = session)
        val fullClub2 = club2.copy(activeSession = null)

        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)
        everySuspend { clubRepository.getClub("c1") } returns Result.success(fullClub1)
        everySuspend { clubRepository.getClub("c2") } returns Result.success(fullClub2)

        val result = useCase(userId)

        val items = result.getOrNull()!!.shelf
        assertEquals(1, items.size)
        assertEquals("Active Book", items[0].bookTitle)
    }

    @Test
    fun `returns failure when member repository fails`() = runTest {
        val userId = "user-123"
        val exception = Exception("Member not found")
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.failure(exception)

        val result = useCase(userId)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { memberRepository.getMemberByUserId(userId) }
    }
}
