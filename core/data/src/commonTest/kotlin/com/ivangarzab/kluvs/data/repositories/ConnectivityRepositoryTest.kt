package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.network.status.OfflineStatus
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame

/**
 * Flipping the status needs the Ktor plugin, which is internal to `:core:network` and covered by
 * its own `OfflineStatusPluginTest`. Here we only check the repository exposes that same flow.
 */
class ConnectivityRepositoryTest {

    private val offlineStatus = OfflineStatus()
    private val repository: ConnectivityRepository = ConnectivityRepositoryImpl(offlineStatus)

    @Test
    fun startsOnline() {
        assertFalse(repository.isOffline.value)
    }

    @Test
    fun exposesTheNetworkStatusFlow() {
        assertSame(offlineStatus.isOffline, repository.isOffline)
    }
}
