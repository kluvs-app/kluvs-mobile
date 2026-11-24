package com.ivangarzab.kluvs.auth.domain

import com.ivangarzab.kluvs.database.KluvsDatabase
import com.ivangarzab.kluvs.database.dao.BookDao
import com.ivangarzab.kluvs.database.dao.BookEnrichmentDao
import com.ivangarzab.kluvs.database.dao.ClubDao
import com.ivangarzab.kluvs.database.dao.DiscussionAttendanceDao
import com.ivangarzab.kluvs.database.dao.DiscussionDao
import com.ivangarzab.kluvs.database.dao.DiscussionNoteDao
import com.ivangarzab.kluvs.database.dao.LikeDao
import com.ivangarzab.kluvs.database.dao.MemberDao
import com.ivangarzab.kluvs.database.dao.ProgressDao
import com.ivangarzab.kluvs.database.dao.ServerDao
import com.ivangarzab.kluvs.database.dao.SessionDao
import com.ivangarzab.kluvs.database.dao.ShelfDao
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SignOutUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var database: KluvsDatabase
    private lateinit var clubDao: ClubDao
    private lateinit var bookEnrichmentDao: BookEnrichmentDao
    private lateinit var progressDao: ProgressDao
    private lateinit var useCase: SignOutUseCase

    @BeforeTest
    fun setup() {
        authRepository = mock<AuthRepository>()

        // Set up mock DAOs that SignOutUseCase will call
        clubDao = mock<ClubDao>()
        val serverDao = mock<ServerDao>()
        val memberDao = mock<MemberDao>()
        val sessionDao = mock<SessionDao>()
        val bookDao = mock<BookDao>()
        bookEnrichmentDao = mock<BookEnrichmentDao>()
        val discussionDao = mock<DiscussionDao>()
        val shelfDao = mock<ShelfDao>()
        val likeDao = mock<LikeDao>()
        progressDao = mock<ProgressDao>()
        val discussionNoteDao = mock<DiscussionNoteDao>()
        val discussionAttendanceDao = mock<DiscussionAttendanceDao>()

        database = mock<KluvsDatabase>()
        every { database.clubDao() } returns clubDao
        every { database.serverDao() } returns serverDao
        every { database.memberDao() } returns memberDao
        every { database.sessionDao() } returns sessionDao
        every { database.bookDao() } returns bookDao
        every { database.bookEnrichmentDao() } returns bookEnrichmentDao
        every { database.discussionDao() } returns discussionDao
        every { database.shelfDao() } returns shelfDao
        every { database.likeDao() } returns likeDao
        every { database.progressDao() } returns progressDao
        every { database.discussionNoteDao() } returns discussionNoteDao
        every { database.discussionAttendanceDao() } returns discussionAttendanceDao

        // Mock the suspend delete methods to return Unit
        everySuspend { clubDao.deleteAll() } returns Unit
        everySuspend { serverDao.deleteAll() } returns Unit
        everySuspend { memberDao.deleteAll() } returns Unit
        everySuspend { memberDao.deleteAllCrossRefs() } returns Unit
        everySuspend { sessionDao.deleteAll() } returns Unit
        everySuspend { bookDao.deleteAll() } returns Unit
        everySuspend { bookEnrichmentDao.deleteAll() } returns Unit
        everySuspend { discussionDao.deleteAll() } returns Unit
        everySuspend { shelfDao.deleteAll() } returns Unit
        everySuspend { likeDao.deleteAll() } returns Unit
        everySuspend { progressDao.deleteAll() } returns Unit
        everySuspend { progressDao.deleteAllQueries() } returns Unit
        everySuspend { discussionNoteDao.deleteAll() } returns Unit
        everySuspend { discussionAttendanceDao.deleteAll() } returns Unit

        useCase = SignOutUseCase(authRepository, database)
    }

    @Test
    fun `returns success when repository succeeds`() = runTest {
        // Given
        everySuspend { authRepository.signOut() } returns Result.success(Unit)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        verifySuspend { authRepository.signOut() }
    }

    @Test
    fun `returns failure when repository fails`() = runTest {
        // Given
        val error = AuthError.UnexpectedError
        everySuspend { authRepository.signOut() } returns Result.failure(error)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
        verifySuspend { authRepository.signOut() }
    }

    @Test
    fun `returns failure with auth error when repository fails with auth error`() = runTest {
        // Given
        val error = AuthError.NoConnection
        everySuspend { authRepository.signOut() } returns Result.failure(error)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals(AuthError.NoConnection, result.exceptionOrNull())
        verifySuspend { authRepository.signOut() }
    }

    @Test
    fun `clears local data even when repository fails`() = runTest {
        // Given: the server sign-out fails, but AuthRepository has already dropped the local session
        everySuspend { authRepository.signOut() } returns Result.failure(AuthError.NoConnection)

        // When
        useCase()

        // Then
        verifySuspend { clubDao.deleteAll() }
    }

    @Test
    fun `clears cached book enrichment on sign out`() = runTest {
        // Given
        everySuspend { authRepository.signOut() } returns Result.success(Unit)

        // When
        useCase()

        // Then
        verifySuspend { bookEnrichmentDao.deleteAll() }
    }

    @Test
    fun `clears cached progress query timestamps on sign out`() = runTest {
        // Given
        everySuspend { authRepository.signOut() } returns Result.success(Unit)

        // When
        useCase()

        // Then: otherwise the next account would get the previous one's "fetched, nothing
        // there" progress results as cache hits
        verifySuspend { progressDao.deleteAllQueries() }
    }
}
