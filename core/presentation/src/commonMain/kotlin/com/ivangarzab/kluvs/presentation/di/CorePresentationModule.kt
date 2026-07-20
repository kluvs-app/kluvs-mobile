package com.ivangarzab.kluvs.presentation.di

import com.ivangarzab.kluvs.presentation.progress.GetSessionProgressUseCase
import com.ivangarzab.kluvs.presentation.progress.SaveProgressUseCase
import com.ivangarzab.kluvs.presentation.util.FormatDateTimeUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val corePresentationModule = module {
    factoryOf(::FormatDateTimeUseCase)
    factoryOf(::GetSessionProgressUseCase)
    factoryOf(::SaveProgressUseCase)
}