package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.data.remote.source.ConfigRemoteDataSource
import com.ivangarzab.kluvs.model.ClientConfig
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfigRepositoryTest {

    private lateinit var remoteDataSource: ConfigRemoteDataSource
    private lateinit var repository: ConfigRepository

    @BeforeTest
    fun setup() {
        remoteDataSource = mock<ConfigRemoteDataSource>()
        repository = ConfigRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getConfig success delegates to remote`() = runTest {
        val config = ClientConfig(flags = emptyMap(), minSupportedVersion = "1.2.0")
        everySuspend { remoteDataSource.getConfig() } returns Result.success(config)

        assertEquals(config, repository.getConfig().getOrNull())
    }

    @Test
    fun `getConfig failure is passed through`() = runTest {
        everySuspend { remoteDataSource.getConfig() } returns Result.failure(Exception("Offline"))

        assertTrue(repository.getConfig().isFailure)
    }
}
