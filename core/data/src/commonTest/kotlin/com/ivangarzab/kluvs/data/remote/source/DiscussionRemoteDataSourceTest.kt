package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.kluvs.api.models.DiscussionCreateRequestDto
import com.ivangarzab.kluvs.api.models.DiscussionDto
import com.ivangarzab.kluvs.api.models.DiscussionUpdateRequestDto
import com.ivangarzab.kluvs.data.remote.api.DiscussionService
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

class DiscussionRemoteDataSourceTest {

    private lateinit var discussionService: DiscussionService
    private lateinit var dataSource: DiscussionRemoteDataSource

    private val discussionDto = DiscussionDto(
        id = "disc-1",
        sessionId = "session-1",
        title = "Chapter 1 discussion",
        scheduledAt = "2026-08-01T18:00:00"
    )

    @BeforeTest
    fun setup() {
        discussionService = mock<DiscussionService>()
        dataSource = DiscussionRemoteDataSourceImpl(discussionService)
    }

    @Test
    fun `createDiscussion maps created discussion to domain`() = runTest {
        val request = DiscussionCreateRequestDto(
            sessionId = "session-1",
            title = "Chapter 1 discussion",
            scheduledAt = "2026-08-01T18:00:00"
        )
        everySuspend { discussionService.create(request) } returns discussionDto

        val result = dataSource.createDiscussion(request)

        assertTrue(result.isSuccess)
        assertEquals("disc-1", result.getOrNull()?.id)
        assertEquals("Chapter 1 discussion", result.getOrNull()?.title)
    }

    @Test
    fun `updateDiscussion maps updated discussion to domain`() = runTest {
        val request = DiscussionUpdateRequestDto(id = "disc-1", title = "Renamed")
        everySuspend { discussionService.update(request) } returns
            discussionDto.copy(title = "Renamed")

        val result = dataSource.updateDiscussion(request)

        assertEquals("Renamed", result.getOrNull()?.title)
    }

    @Test
    fun `deleteDiscussion delegates to service`() = runTest {
        everySuspend { discussionService.delete("disc-1") } returns Unit

        val result = dataSource.deleteDiscussion("disc-1")

        assertTrue(result.isSuccess)
        verifySuspend { discussionService.delete("disc-1") }
    }

    @Test
    fun `deleteDiscussion returns failure when service throws`() = runTest {
        everySuspend { discussionService.delete("disc-1") } throws Exception("Forbidden")

        val result = dataSource.deleteDiscussion("disc-1")

        assertTrue(result.isFailure)
    }
}
