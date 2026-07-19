package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.data.local.cache.CachePolicy
import com.ivangarzab.kluvs.data.local.source.ClubLocalDataSource
import com.ivangarzab.kluvs.data.remote.source.ClubRemoteDataSource
import com.ivangarzab.kluvs.model.Club
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
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ClubRepositoryTest {

    private lateinit var remoteDataSource: ClubRemoteDataSource
    private lateinit var localDataSource: ClubLocalDataSource
    private lateinit var cachePolicy: CachePolicy
    private lateinit var repository: ClubRepository

    @BeforeTest
    fun setup() {
        remoteDataSource = mock<ClubRemoteDataSource>()
        localDataSource = mock<ClubLocalDataSource>()
        cachePolicy = CachePolicy()
        repository = ClubRepositoryImpl(remoteDataSource, localDataSource, cachePolicy)

        // Default behavior: cache miss (return null)
        everySuspend { localDataSource.getClub(any()) } returns null
        everySuspend { localDataSource.getLastFetchedAt(any()) } returns null
        everySuspend { localDataSource.insertClub(any()) } returns Unit
        everySuspend { localDataSource.deleteClub(any()) } returns Unit
    }

    // ========================================
    // GET CLUB
    // ========================================

    @Test
    fun `getClub success returns Club with nested data`() = runTest {
        val clubId = "club-123"
        val serverId = "server-456"
        val expectedClub = Club(
            id = clubId,
            name = "Sci-Fi Club",
            serverId = serverId,
            discordChannel = "#sci-fi"
        )
        everySuspend { remoteDataSource.getClub(clubId, serverId) } returns Result.success(expectedClub)

        val result = repository.getClub(clubId, serverId)

        assertTrue(result.isSuccess)
        assertEquals(expectedClub, result.getOrNull())
        assertEquals("Sci-Fi Club", result.getOrNull()?.name)
        assertEquals("#sci-fi", result.getOrNull()?.discordChannel)
        verifySuspend { remoteDataSource.getClub(clubId, serverId) }
    }

    @Test
    fun `getClub failure returns Result failure`() = runTest {
        val clubId = "club-123"
        val serverId = "server-456"
        val exception = Exception("Club not found")
        everySuspend { remoteDataSource.getClub(clubId, serverId) } returns Result.failure(exception)

        val result = repository.getClub(clubId, serverId)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { remoteDataSource.getClub(clubId, serverId) }
    }

    @Test
    fun `getClub with non-existent club returns failure`() = runTest {
        val clubId = "non-existent"
        val serverId = "server-456"
        val exception = Exception("Club not found")
        everySuspend { remoteDataSource.getClub(clubId, serverId) } returns Result.failure(exception)

        val result = repository.getClub(clubId, serverId)

        assertTrue(result.isFailure)
        verifySuspend { remoteDataSource.getClub(clubId, serverId) }
    }

    // ========================================
    // CREATE CLUB
    // ========================================

    @Test
    fun `createClub success creates club with Discord channel`() = runTest {
        val clubName = "New Club"
        val serverId = "server-456"
        val discordChannel = "#new-club"
        val expectedClub = Club(
            id = "club-new",
            name = clubName,
            serverId = serverId,
            discordChannel = discordChannel
        )
        everySuspend { remoteDataSource.createClub(any()) } returns Result.success(expectedClub)

        val result = repository.createClub(clubName, "member-1", "Creator", 0, serverId, discordChannel)

        assertTrue(result.isSuccess)
        assertEquals(expectedClub, result.getOrNull())
        assertEquals(clubName, result.getOrNull()?.name)
        assertEquals(discordChannel, result.getOrNull()?.discordChannel)
        verifySuspend { remoteDataSource.createClub(any()) }
    }

    @Test
    fun `createClub success creates club without Discord channel`() = runTest {
        val clubName = "Simple Club"
        val serverId = "server-456"
        val expectedClub = Club(
            id = "club-new",
            name = clubName,
            serverId = serverId,
            discordChannel = null
        )
        everySuspend { remoteDataSource.createClub(any()) } returns Result.success(expectedClub)

        val result = repository.createClub(clubName, "member-1", "Creator", 0, serverId, null)

        assertTrue(result.isSuccess)
        assertEquals(expectedClub, result.getOrNull())
        assertEquals(clubName, result.getOrNull()?.name)
        assertEquals(null, result.getOrNull()?.discordChannel)
        verifySuspend { remoteDataSource.createClub(any()) }
    }

    @Test
    fun `createClub failure returns Result failure`() = runTest {
        val exception = Exception("Failed to create club")
        everySuspend { remoteDataSource.createClub(any()) } returns Result.failure(exception)

        val result = repository.createClub("New Club", "member-1", "Creator", 0, "server-456", null)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { remoteDataSource.createClub(any()) }
    }

    // ========================================
    // UPDATE CLUB
    // ========================================

    @Test
    fun `updateClub with name and Discord channel updates both`() = runTest {
        val clubId = "club-123"
        val serverId = "server-456"
        val newName = "Updated Club"
        val newChannel = "#updated"
        val expectedClub = Club(
            id = clubId,
            name = newName,
            serverId = serverId,
            discordChannel = newChannel
        )
        everySuspend { remoteDataSource.updateClub(any()) } returns Result.success(expectedClub)

        val result = repository.updateClub(clubId, serverId, newName, newChannel)

        assertTrue(result.isSuccess)
        assertEquals(newName, result.getOrNull()?.name)
        assertEquals(newChannel, result.getOrNull()?.discordChannel)
        verifySuspend { remoteDataSource.updateClub(any()) }
    }

    @Test
    fun `updateClub with null name does not update name`() = runTest {
        val clubId = "club-123"
        val serverId = "server-456"
        val expectedClub = Club(
            id = clubId,
            name = "Unchanged",
            serverId = serverId,
            discordChannel = "#updated"
        )
        everySuspend { remoteDataSource.updateClub(any()) } returns Result.success(expectedClub)

        val result = repository.updateClub(clubId, serverId, null, "#updated")

        assertTrue(result.isSuccess)
        assertEquals("Unchanged", result.getOrNull()?.name)
        assertEquals("#updated", result.getOrNull()?.discordChannel)
        verifySuspend { remoteDataSource.updateClub(any()) }
    }

    @Test
    fun `updateClub with null Discord channel does not update Discord channel`() = runTest {
        val clubId = "club-123"
        val serverId = "server-456"
        val expectedClub = Club(
            id = clubId,
            name = "Updated",
            serverId = serverId,
            discordChannel = "#original"
        )
        everySuspend { remoteDataSource.updateClub(any()) } returns Result.success(expectedClub)

        val result = repository.updateClub(clubId, serverId, "Updated", null)

        assertTrue(result.isSuccess)
        assertEquals("Updated", result.getOrNull()?.name)
        assertEquals("#original", result.getOrNull()?.discordChannel)
        verifySuspend { remoteDataSource.updateClub(any()) }
    }

    @Test
    fun `updateClub failure returns Result failure`() = runTest {
        val exception = Exception("Failed to update club")
        everySuspend { remoteDataSource.updateClub(any()) } returns Result.failure(exception)

        val result = repository.updateClub("club-123", "server-456", "Updated", null)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { remoteDataSource.updateClub(any()) }
    }

    // ========================================
    // DELETE CLUB
    // ========================================

    @Test
    fun `deleteClub success returns success message`() = runTest {
        val clubId = "club-123"
        val serverId = "server-456"
        val successMessage = "Club deleted successfully"
        everySuspend { remoteDataSource.deleteClub(clubId, serverId) } returns Result.success(successMessage)

        val result = repository.deleteClub(clubId, serverId)

        assertTrue(result.isSuccess)
        assertEquals(successMessage, result.getOrNull())
        verifySuspend { remoteDataSource.deleteClub(clubId, serverId) }
    }

    @Test
    fun `deleteClub failure returns Result failure`() = runTest {
        val clubId = "club-123"
        val serverId = "server-456"
        val exception = Exception("Failed to delete club")
        everySuspend { remoteDataSource.deleteClub(clubId, serverId) } returns Result.failure(exception)

        val result = repository.deleteClub(clubId, serverId)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { remoteDataSource.deleteClub(clubId, serverId) }
    }

    @Test
    fun `deleteClub with non-existent club returns failure`() = runTest {
        val clubId = "non-existent"
        val serverId = "server-456"
        val exception = Exception("Club not found")
        everySuspend { remoteDataSource.deleteClub(clubId, serverId) } returns Result.failure(exception)

        val result = repository.deleteClub(clubId, serverId)

        assertTrue(result.isFailure)
        verifySuspend { remoteDataSource.deleteClub(clubId, serverId) }
    }
}
