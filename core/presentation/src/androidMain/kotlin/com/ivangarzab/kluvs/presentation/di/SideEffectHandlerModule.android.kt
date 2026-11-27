package com.ivangarzab.kluvs.presentation.di

import com.ivangarzab.kluvs.presentation.sideeffect.AndroidSideEffectHandler
import com.ivangarzab.kluvs.presentation.sideeffect.SideEffectHandler
import org.koin.dsl.module

actual val sideEffectHandlerModule = module {
    // Needs the application Context, registered by androidContext() in App
    single<SideEffectHandler> { AndroidSideEffectHandler(get()) }
}
