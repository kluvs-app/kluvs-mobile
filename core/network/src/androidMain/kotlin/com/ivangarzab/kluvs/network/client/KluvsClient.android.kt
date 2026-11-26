package com.ivangarzab.kluvs.network.client

import android.content.Context
import org.koin.dsl.module

internal actual val kluvsClientModule = module {
    single {
        val context = get<Context>()
        // The PackageInfoFlags overload is API 33+; minSdk is 24
        @Suppress("DEPRECATION")
        val versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        KluvsClient(platform = "android", version = versionName.orEmpty())
    }
}
