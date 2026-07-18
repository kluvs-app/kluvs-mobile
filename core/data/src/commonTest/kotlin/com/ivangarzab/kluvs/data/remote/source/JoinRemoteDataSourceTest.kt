package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.kluvs.api.models.ClubPreviewDto
import com.ivangarzab.kluvs.api.models.JoinPreviewResponseDto
import com.ivangarzab.kluvs.api.models.JoinRequestDto
import com.ivangarzab.kluvs.api.models.JoinResponseDto
import com.ivangarzab.kluvs.data.remote.api.JoinService
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JoinRemoteDataSourceTest {

    private lateinit var joinService: JoinService
    private lateinit var dataSource: JoinRemoteDataSource

    @BeforeTest
    fun setup() {
        joinService = mock<JoinService>()
        dataSource = JoinRemoteDataSourceImpl(joinService)
    }

    @Test
    fun `previewInvite maps club preview to domain`() = runTest {
        everySuspend { joinService.preview("token-123") } returns JoinPreviewResponseDto(
            valid = true,
            club = ClubPreviewDto(id = "club-1", name = "Freaks & Geeks")
        )

        val result = dataSource.previewInvite("token-123")

        assertTrue(result.isSuccess)
        assertEquals("Freaks & Geeks", result.getOrNull()?.name)
    }

    @Test
    fun `previewInvite fails for invalid token response`() = runTest {
        everySuspend { joinService.preview("bad-token") } returns
            JoinPreviewResponseDto(valid = false)

        val result = dataSource.previewInvite("bad-token")

        assertTrue(result.isFailure)
    }

    @Test
    fun `previewInvite returns failure when service throws`() = runTest {
        everySuspend { joinService.preview("bad-token") } throws Exception("Not found")

        val result = dataSource.previewInvite("bad-token")

        assertTrue(result.isFailure)
    }

    @Test
    fun `joinClub returns joined club ID`() = runTest {
        val request = JoinRequestDto(token = "token-123")
        everySuspend { joinService.join(request) } returns JoinResponseDto(
            success = true,
            clubId = "club-1"
        )

        val result = dataSource.joinClub(request)

        assertEquals("club-1", result.getOrNull())
    }

    @Test
    fun `joinClub fails when response carries no club ID`() = runTest {
        val request = JoinRequestDto(token = "token-123")
        everySuspend { joinService.join(request) } returns JoinResponseDto(success = true)

        val result = dataSource.joinClub(request)

        assertTrue(result.isFailure)
    }
}
