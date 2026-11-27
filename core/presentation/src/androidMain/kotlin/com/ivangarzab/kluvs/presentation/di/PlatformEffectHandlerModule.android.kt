package com.ivangarzab.kluvs.presentation.di

import com.ivangarzab.kluvs.presentation.effect.AndroidPlatformEffectHandler
import com.ivangarzab.kluvs.presentation.effect.PlatformEffectHandler
import org.koin.dsl.module

actual val platformEffectHandlerModule = module {
    // Needs the application Context, registered by androidContext() in App
    single<PlatformEffectHandler> { AndroidPlatformEffectHandler(get()) }
}
