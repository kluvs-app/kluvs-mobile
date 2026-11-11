package com.ivangarzab.kluvs.app

import com.ivangarzab.kluvs.data.repositories.ConnectivityRepository
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityCoordinatorHelperTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = CoroutineScope(testDispatcher + Job())
    private val offlineFlow = MutableStateFlow(false)
    private lateinit var helper: ConnectivityCoordinatorHelper

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val connectivityRepository = mock<ConnectivityRepository> {
            every { isOffline } returns offlineFlow
        }
        startKoin {
            modules(
                module {
                    single<ConnectivityCoordinator> { ConnectivityCoordinator(connectivityRepository) }
                    single<CoroutineScope> { testScope }
                }
            )
        }
        helper = ConnectivityCoordinatorHelper()
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        testScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `observeIsOffline reports the current value then changes`() {
        val received = mutableListOf<Boolean>()
        val closeable = helper.observeIsOffline { received.add(it) }

        assertEquals(listOf(false), received)

        offlineFlow.value = true

        assertEquals(listOf(false, true), received)

        closeable.close()
    }
}
