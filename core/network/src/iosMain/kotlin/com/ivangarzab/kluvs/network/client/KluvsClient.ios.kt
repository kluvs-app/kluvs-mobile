package com.ivangarzab.kluvs.network.client

import org.koin.dsl.module
import platform.Foundation.NSBundle

internal actual val kluvsClientModule = module {
    single {
        val version = NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String
        KluvsClient(platform = "ios", version = version.orEmpty())
    }
}
