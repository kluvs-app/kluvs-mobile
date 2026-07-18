package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.api.models.JoinRequestDto
import com.ivangarzab.kluvs.data.remote.source.JoinRemoteDataSource
import com.ivangarzab.kluvs.model.ClubPreview
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JoinRepositoryTest {

    private lateinit var remoteDataSource: JoinRemoteDataSource
    private lateinit var repository: JoinRepository

    private val testClubPreview = ClubPreview(
        id = "club-1",
        name = "Freaks & Geeks"
    )

    @BeforeTest
    fun setup() {
        remoteDataSource = mock<JoinRemoteDataSource>()
        repository = JoinRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `previewInvite success delegates to remote`() = runTest {
        everySuspend { remoteDataSource.previewInvite("token-123") } returns Result.success(testClubPreview)

        val result = repository.previewInvite("token-123")

        assertTrue(result.isSuccess)
        assertEquals(testClubPreview, result.getOrNull())
        verifySuspend { remoteDataSource.previewInvite("token-123") }
    }

    @Test
    fun `joinClub success delegates to remote with mapped request`() = runTest {
        everySuspend { remoteDataSource.joinClub(any()) } returns Result.success("club-1")

        val result = repository.joinClub("token-123")

        assertTrue(result.isSuccess)
        assertEquals("club-1", result.getOrNull())
        verifySuspend { remoteDataSource.joinClub(JoinRequestDto(token = "token-123")) }
    }
}
