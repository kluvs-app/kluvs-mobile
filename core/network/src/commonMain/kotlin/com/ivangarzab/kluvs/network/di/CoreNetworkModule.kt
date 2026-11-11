package com.ivangarzab.kluvs.network.di

import com.ivangarzab.kluvs.network.BuildKonfig
import com.ivangarzab.kluvs.network.status.ConnectivityProbe
import com.ivangarzab.kluvs.network.status.OfflineStatus
import com.ivangarzab.kluvs.network.status.OfflineStatusPlugin
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.storage.Storage
import org.koin.dsl.module

@OptIn(SupabaseInternal::class)
val coreNetworkModule = module {
    single { OfflineStatus(probe = ConnectivityProbe()) }

    single<SupabaseClient> {
        val offlineStatus = get<OfflineStatus>()
        createSupabaseClient(
            supabaseUrl = BuildKonfig.SUPABASE_URL,
            supabaseKey = BuildKonfig.SUPABASE_KEY
        ) {
            install(Functions)
            install(Auth)
            install(Storage)

            // httpConfig is supabase-kt's only hook into the underlying Ktor client, and is marked
            // @SupabaseInternal. OfflineStatusPluginTest builds a real client with it, so an
            // upgrade that changes or removes it fails there first.
            httpConfig {
                install(OfflineStatusPlugin) { status = offlineStatus }
            }
        }
    }
}
