package com.ivangarzab.kluvs.presentation.di

import com.ivangarzab.kluvs.presentation.sideeffect.IosSideEffectHandler
import com.ivangarzab.kluvs.presentation.sideeffect.SideEffectHandler
import org.koin.dsl.module

actual val sideEffectHandlerModule = module {
    single<SideEffectHandler> { IosSideEffectHandler() }
}
