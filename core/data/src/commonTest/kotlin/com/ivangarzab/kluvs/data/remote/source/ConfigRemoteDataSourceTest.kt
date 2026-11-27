package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.kluvs.api.models.ClientConfigDto
import com.ivangarzab.kluvs.data.remote.api.ConfigService
import com.ivangarzab.kluvs.model.ClientConfig
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfigRemoteDataSourceTest {

    private lateinit var configService: ConfigService
    private lateinit var dataSource: ConfigRemoteDataSource

    @BeforeTest
    fun setup() {
        configService = mock<ConfigService>()
        dataSource = ConfigRemoteDataSourceImpl(configService)
    }

    @Test
    fun `getConfig maps config to domain`() = runTest {
        everySuspend { configService.get() } returns
            ClientConfigDto(flags = mapOf("new_shape" to true), minSupportedVersion = "1.2.0")

        val result = dataSource.getConfig()

        assertEquals(
            ClientConfig(flags = mapOf("new_shape" to true), minSupportedVersion = "1.2.0"),
            result.getOrNull()
        )
    }

    @Test
    fun `getConfig keeps a null minimum`() = runTest {
        everySuspend { configService.get() } returns
            ClientConfigDto(flags = emptyMap(), minSupportedVersion = null)

        val result = dataSource.getConfig()

        assertEquals(null, result.getOrNull()?.minSupportedVersion)
    }

    @Test
    fun `getConfig returns failure when service throws`() = runTest {
        everySuspend { configService.get() } throws Exception("Offline")

        val result = dataSource.getConfig()

        assertTrue(result.isFailure)
    }
}
