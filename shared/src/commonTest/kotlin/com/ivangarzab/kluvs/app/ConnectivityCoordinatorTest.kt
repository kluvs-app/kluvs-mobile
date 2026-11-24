package com.ivangarzab.kluvs.app

import com.ivangarzab.kluvs.data.repositories.ConnectivityRepository
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConnectivityCoordinatorTest {

    private val offlineFlow = MutableStateFlow(false)
    private val connectivityRepository = mock<ConnectivityRepository> {
        every { isOffline } returns offlineFlow
    }
    private val coordinator = ConnectivityCoordinator(connectivityRepository)

    @Test
    fun `starts online`() {
        assertFalse(coordinator.isOffline.value)
    }

    @Test
    fun `isOffline mirrors the connectivity repository`() {
        offlineFlow.value = true
        assertTrue(coordinator.isOffline.value)

        offlineFlow.value = false
        assertFalse(coordinator.isOffline.value)
    }
}
