package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.kluvs.api.models.BookDto
import com.ivangarzab.kluvs.api.models.DeleteResponseDto
import com.ivangarzab.kluvs.api.models.DiscussionDto
import com.ivangarzab.kluvs.api.models.SessionCreateRequestDto
import com.ivangarzab.kluvs.api.models.SessionCreateResponseDto
import com.ivangarzab.kluvs.api.models.SessionDto
import com.ivangarzab.kluvs.api.models.SessionUpdateRequestDto
import com.ivangarzab.kluvs.api.models.UpdateSession200ResponseDto
import com.ivangarzab.kluvs.data.remote.api.SessionService
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SessionRemoteDataSourceTest {

    private lateinit var sessionService: SessionService
    private lateinit var dataSource: SessionRemoteDataSource

    @BeforeTest
    fun setup() {
        sessionService = mock<SessionService>()
        dataSource = SessionRemoteDataSourceImpl(sessionService)
    }

    @Test
    fun `getSession success returns mapped Session domain model`() = runTest {
        // Given: Service returns the generated SessionDto (GET /session?id= now documented)
        val dto = SessionDto(
            id = "session-1",
            clubId = "club-1",
            status = SessionDto.Status.active,
            book = BookDto(id = 1, title = "The Hobbit", author = "Tolkien"),
            dueDate = "2024-12-31T23:59:59",
            discussions = listOf(
                DiscussionDto(id = "disc-1", sessionId = "session-1", title = "Chapter 1", scheduledAt = "2024-06-15T18:00:00", location = "Discord")
            )
        )

        everySuspend { sessionService.get("session-1") } returns dto

        // When: Getting session
        val result = dataSource.getSession("session-1")

        // Then: Result is success with mapped domain model
        assertTrue(result.isSuccess)
        val session = result.getOrNull()!!
        assertEquals("session-1", session.id)
        assertEquals("club-1", session.clubId)
        assertEquals("The Hobbit", session.book.title)
        assertEquals(1, session.discussions.size)

        verifySuspend { sessionService.get("session-1") }
    }

    @Test
    fun `getSession failure returns Result failure`() = runTest {
        // Given: Service throws exception
        val exception = Exception("Session not found")
        everySuspend { sessionService.get("invalid") } throws exception

        // When: Getting session
        val result = dataSource.getSession("invalid")

        // Then: Result is failure
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())

        verifySuspend { sessionService.get("invalid") }
    }

    @Test
    fun `createSession success returns created Session`() = runTest {
        // Given: Service returns success response (hand-written wrapper; POST /session is undocumented)
        val request = SessionCreateRequestDto(
            clubId = "club-1",
            bookId = 2,
            dueDate = "2025-06-30T00:00:00"
        )

        val responseDto = SessionCreateResponseDto(
            success = true,
            message = "Created",
            session = SessionDto(
                id = "session-2",
                clubId = "club-1",
                status = SessionDto.Status.active,
                book = BookDto(id = 2, title = "New Book", author = "New Author"),
                dueDate = "2025-06-30T00:00:00",
                discussions = emptyList()
            )
        )

        everySuspend { sessionService.create(request) } returns responseDto

        // When: Creating session
        val result = dataSource.createSession(request)

        // Then: Result is success
        assertTrue(result.isSuccess)
        val session = result.getOrNull()!!
        assertEquals("session-2", session.id)
        assertEquals("New Book", session.book.title)

        verifySuspend { sessionService.create(request) }
    }

    @Test
    fun `createSession with null session in response returns failure`() = runTest {
        // Given: Service returns response without session
        val request = SessionCreateRequestDto(
            clubId = "club-1",
            bookId = 1
        )

        val responseDto = SessionCreateResponseDto(
            success = true,
            message = "Created but no session returned",
            session = null
        )

        everySuspend { sessionService.create(request) } returns responseDto

        // When: Creating session
        val result = dataSource.createSession(request)

        // Then: Result is failure
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("no session returned") == true)

        verifySuspend { sessionService.create(request) }
    }

    @Test
    fun `updateSession success returns Unit since PUT response carries no session data`() = runTest {
        // Given: Service returns the general-update branch response
        val request = SessionUpdateRequestDto(
            id = "session-1",
            dueDate = "2025-12-31T23:59:59"
        )

        val responseDto = UpdateSession200ResponseDto(
            success = true,
            message = "Updated"
        )

        everySuspend { sessionService.update(request) } returns responseDto

        // When: Updating session
        val result = dataSource.updateSession(request)

        // Then: Result is success with no payload
        assertTrue(result.isSuccess)

        verifySuspend { sessionService.update(request) }
    }

    @Test
    fun `updateSession with success false returns failure`() = runTest {
        // Given: Service returns a failure-flagged response
        val request = SessionUpdateRequestDto(id = "session-1", finish = true)

        val responseDto = UpdateSession200ResponseDto(
            success = false,
            message = "Session already finished"
        )

        everySuspend { sessionService.update(request) } returns responseDto

        // When: Updating session
        val result = dataSource.updateSession(request)

        // Then: Result is failure
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Session already finished") == true)

        verifySuspend { sessionService.update(request) }
    }

    @Test
    fun `deleteSession success returns success message`() = runTest {
        // Given: Service returns success response
        val response = DeleteResponseDto(
            success = true,
            message = "Session deleted"
        )

        everySuspend { sessionService.delete("session-1") } returns response

        // When: Deleting session
        val result = dataSource.deleteSession("session-1")

        // Then: Result is success
        assertTrue(result.isSuccess)
        assertEquals("Session deleted", result.getOrNull())

        verifySuspend { sessionService.delete("session-1") }
    }

    @Test
    fun `deleteSession with success false returns failure`() = runTest {
        // Given: Service returns failure response
        val response = DeleteResponseDto(
            success = false,
            message = "Cannot delete active session"
        )

        everySuspend { sessionService.delete("session-1") } returns response

        // When: Deleting session
        val result = dataSource.deleteSession("session-1")

        // Then: Result is failure
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Cannot delete active session") == true)

        verifySuspend { sessionService.delete("session-1") }
    }
}
