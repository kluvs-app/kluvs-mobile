package com.ivangarzab.kluvs.network.client

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.Headers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(SupabaseInternal::class)
class KluvsClientHeaderTest {

    @Test
    fun headerValueIsPlatformSlashVersion() {
        assertEquals("android/1.2.3", KluvsClient(platform = "android", version = "1.2.3").headerValue)
    }

    @Test
    fun supabaseFunctionCallSendsKluvsClientHeader() = runTest {
        var sentHeaders: Headers? = null
        val supabase = createSupabaseClient(
            supabaseUrl = "https://example.supabase.co",
            supabaseKey = "test-key",
        ) {
            httpEngine = MockEngine { request ->
                sentHeaders = request.headers
                respond("{}")
            }
            install(Functions)
            httpConfig { sendKluvsClientHeader(KluvsClient(platform = "ios", version = "0.1.0")) }
        }

        supabase.functions.invoke("club")

        assertEquals(listOf("ios/0.1.0"), sentHeaders?.getAll(KLUVS_CLIENT_HEADER))
    }
}
