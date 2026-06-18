package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.kluvs.api.models.ClubDto
import com.ivangarzab.kluvs.api.models.ClubMemberDto
import com.ivangarzab.kluvs.api.models.ClubCreateResponseDto
import com.ivangarzab.kluvs.api.models.ClubCreateRequestDto
import com.ivangarzab.kluvs.api.models.ClubUpdateResponseDto
import com.ivangarzab.kluvs.api.models.ClubUpdateRequestDto
import com.ivangarzab.kluvs.data.remote.api.ClubService
import com.ivangarzab.kluvs.api.models.DeleteResponseDto
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

class ClubRemoteDataSourceTest {

    private lateinit var clubService: ClubService
    private lateinit var dataSource: ClubRemoteDataSource

    @BeforeTest
    fun setup() {
        clubService = mock<ClubService>()
        dataSource = ClubRemoteDataSourceImpl(clubService)
    }

    @Test
    fun `getClub success returns mapped Club domain model`() = runTest {
        // Given: Service returns ClubDto
        val dto = ClubDto(
            id = "club-1",
            name = "Test Club",
            discordChannel = "123456789",
            serverId = "987654321",
            members = listOf(
                ClubMemberDto(
                    id = 1,
                    name = "John",
                    booksRead = 5,
                    role = ClubMemberDto.Role.owner
                )
            ),
            activeSession = null,
            pastSessions = emptyList(),
            shameList = listOf(2)
        )

        everySuspend { clubService.get("club-1", "987654321") } returns dto

        // When: Getting club from data source
        val result = dataSource.getClub("club-1", "987654321")

        // Then: Result is success with mapped domain model
        assertTrue(result.isSuccess)
        val club = result.getOrNull()!!
        assertEquals("club-1", club.id)
        assertEquals("Test Club", club.name)
        assertEquals(1, club.members?.size)
        assertEquals("John", club.members?.first()?.member?.name)
        assertEquals(1, club.shameList.size)

        verifySuspend { clubService.get("club-1", "987654321") }
    }

    @Test
    fun `getClub failure returns Result failure`() = runTest {
        // Given: Service throws exception
        val exception = Exception("Network error")
        everySuspend { clubService.get("club-1", "987654321") } throws exception

        // When: Getting club from data source
        val result = dataSource.getClub("club-1", "987654321")

        // Then: Result is failure with exception
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())

        verifySuspend { clubService.get("club-1", "987654321") }
    }

    @Test
    fun `getClubByChannel success returns mapped Club`() = runTest {
        // Given: Service returns club by channel
        val dto = ClubDto(
            id = "club-2",
            name = "Channel Club",
            discordChannel = "555666777",
            serverId = "987654321",
            members = emptyList(),
            activeSession = null,
            pastSessions = emptyList(),
            shameList = emptyList()
        )

        everySuspend { clubService.getByChannel("555666777", "987654321") } returns dto

        // When: Getting club by channel
        val result = dataSource.getClubByChannel("555666777", "987654321")

        // Then: Result is success
        assertTrue(result.isSuccess)
        assertEquals("club-2", result.getOrNull()?.id)

        verifySuspend { clubService.getByChannel("555666777", "987654321") }
    }

    @Test
    fun `createClub success returns created Club`() = runTest {
        // Given: Service returns success response
        val request = ClubCreateRequestDto(
            name = "New Club",
            discordChannel = "111222333",
            serverId = "987654321"
        )

        val responseDto = ClubCreateResponseDto(
            success = true,
            message = "Created",
            club = ClubDto(
                id = "new-club",
                name = "New Club",
                discordChannel = "111222333",
                serverId = "987654321"
            )
        )

        everySuspend { clubService.create(request) } returns responseDto

        // When: Creating club
        val result = dataSource.createClub(request)

        // Then: Result is success with basic club info
        assertTrue(result.isSuccess)
        val club = result.getOrNull()!!
        assertEquals("new-club", club.id)
        assertEquals("New Club", club.name)

        verifySuspend { clubService.create(request) }
    }

    @Test
    fun `updateClub success returns updated Club`() = runTest {
        // Given: Service returns success response
        val request = ClubUpdateRequestDto(
            id = "club-1",
            serverId = "987654321",
            name = "Updated Name"
        )

        val responseDto = ClubUpdateResponseDto(
            success = true,
            message = "Updated",
            club = ClubDto(
                id = "club-1",
                name = "Updated Name",
                discordChannel = "123456789",
                serverId = "987654321"
            ),
            clubUpdated = true
        )

        everySuspend { clubService.update(request) } returns responseDto

        // When: Updating club
        val result = dataSource.updateClub(request)

        // Then: Result is success
        assertTrue(result.isSuccess)
        assertEquals("Updated Name", result.getOrNull()?.name)

        verifySuspend { clubService.update(request) }
    }

    @Test
    fun `deleteClub success returns success message`() = runTest {
        // Given: Service returns success response
        val response = DeleteResponseDto(
            success = true,
            message = "Club deleted successfully"
        )

        everySuspend { clubService.delete("club-1", "987654321") } returns response

        // When: Deleting club
        val result = dataSource.deleteClub("club-1", "987654321")

        // Then: Result is success with message
        assertTrue(result.isSuccess)
        assertEquals("Club deleted successfully", result.getOrNull())

        verifySuspend { clubService.delete("club-1", "987654321") }
    }

    @Test
    fun `deleteClub with success false returns failure`() = runTest {
        // Given: Service returns failure response
        val response = DeleteResponseDto(
            success = false,
            message = "Delete failed"
        )

        everySuspend { clubService.delete("club-1", "987654321") } returns response

        // When: Deleting club
        val result = dataSource.deleteClub("club-1", "987654321")

        // Then: Result is failure
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Delete failed") == true)

        verifySuspend { clubService.delete("club-1", "987654321") }
    }
}
