package com.ivangarzab.kluvs.data.domain

import com.ivangarzab.kluvs.data.repositories.ConfigRepository
import com.ivangarzab.kluvs.model.ClientConfig
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckUpdateRequiredUseCaseTest {

    private lateinit var configRepository: ConfigRepository

    @BeforeTest
    fun setup() {
        configRepository = mock<ConfigRepository>()
    }

    private fun givenMinimum(minimum: String?) {
        everySuspend { configRepository.getConfig() } returns
            Result.success(ClientConfig(flags = emptyMap(), minSupportedVersion = minimum))
    }

    private suspend fun check(installed: String) =
        CheckUpdateRequiredUseCase(configRepository, installedVersion = installed)()

    @Test
    fun `requires update when installed version is below minimum`() = runTest {
        givenMinimum("1.2.0")
        assertTrue(check("1.1.9"))
    }

    @Test
    fun `does not require update when installed version equals minimum`() = runTest {
        givenMinimum("1.2.0")
        assertFalse(check("1.2.0"))
    }

    @Test
    fun `does not require update when installed version is above minimum`() = runTest {
        givenMinimum("1.2.0")
        assertFalse(check("1.10.0"))
    }

    @Test
    fun `fails open when no minimum is set`() = runTest {
        givenMinimum(null)
        assertFalse(check("0.0.1"))
    }

    @Test
    fun `fails open when the config request fails`() = runTest {
        everySuspend { configRepository.getConfig() } returns Result.failure(Exception("Offline"))
        assertFalse(check("0.0.1"))
    }

    @Test
    fun `fails open when the installed version is unparseable`() = runTest {
        givenMinimum("1.2.0")
        assertFalse(check(""))
    }

    @Test
    fun `fails open when the minimum is unparseable`() = runTest {
        givenMinimum("latest")
        assertFalse(check("0.0.1"))
    }
}
