package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.api.models.DiscussionCreateRequestDto
import com.ivangarzab.kluvs.api.models.DiscussionUpdateRequestDto
import com.ivangarzab.kluvs.data.remote.source.DiscussionRemoteDataSource
import com.ivangarzab.kluvs.model.Discussion
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
import kotlin.test.assertTrue

class DiscussionRepositoryTest {

    private lateinit var remoteDataSource: DiscussionRemoteDataSource
    private lateinit var repository: DiscussionRepository

    private val testDiscussion = Discussion(
        id = "discussion-1",
        sessionId = "session-1",
        title = "Introduction",
        date = LocalDateTime.parse("2026-08-01T18:00:00")
    )

    @BeforeTest
    fun setup() {
        remoteDataSource = mock<DiscussionRemoteDataSource>()
        repository = DiscussionRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `createDiscussion success delegates to remote with mapped request`() = runTest {
        everySuspend { remoteDataSource.createDiscussion(any()) } returns Result.success(testDiscussion)
        val date = LocalDateTime.parse("2026-08-01T18:00:00")

        val result = repository.createDiscussion(
            sessionId = "session-1",
            title = "Introduction",
            date = date,
            location = "Discord Voice"
        )

        assertTrue(result.isSuccess)
        assertEquals(testDiscussion, result.getOrNull())
        verifySuspend {
            remoteDataSource.createDiscussion(
                DiscussionCreateRequestDto(
                    sessionId = "session-1",
                    title = "Introduction",
                    scheduledAt = "2026-08-01T18:00",
                    location = "Discord Voice"
                )
            )
        }
    }

    @Test
    fun `updateDiscussion success delegates to remote with mapped request`() = runTest {
        everySuspend { remoteDataSource.updateDiscussion(any()) } returns Result.success(testDiscussion)
        val date = LocalDateTime.parse("2026-08-01T18:00:00")

        val result = repository.updateDiscussion(
            discussionId = "discussion-1",
            title = "New Title",
            date = date,
            location = "New Room"
        )

        assertTrue(result.isSuccess)
        assertEquals(testDiscussion, result.getOrNull())
        verifySuspend {
            remoteDataSource.updateDiscussion(
                DiscussionUpdateRequestDto(
                    id = "discussion-1",
                    title = "New Title",
                    scheduledAt = "2026-08-01T18:00",
                    location = "New Room"
                )
            )
        }
    }

    @Test
    fun `deleteDiscussion success delegates to remote`() = runTest {
        everySuspend { remoteDataSource.deleteDiscussion("discussion-1") } returns Result.success(Unit)

        val result = repository.deleteDiscussion("discussion-1")

        assertTrue(result.isSuccess)
        verifySuspend { remoteDataSource.deleteDiscussion("discussion-1") }
    }
}
