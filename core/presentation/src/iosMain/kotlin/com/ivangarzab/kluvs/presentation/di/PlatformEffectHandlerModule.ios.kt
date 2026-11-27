package com.ivangarzab.kluvs.presentation.di

import com.ivangarzab.kluvs.presentation.effect.IosPlatformEffectHandler
import com.ivangarzab.kluvs.presentation.effect.PlatformEffectHandler
import org.koin.dsl.module

actual val platformEffectHandlerModule = module {
    single<PlatformEffectHandler> { IosPlatformEffectHandler() }
}
