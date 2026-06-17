package com.ivangarzab.kluvs.settings.di

import com.ivangarzab.kluvs.settings.domain.GetEditableProfileUseCase
import com.ivangarzab.kluvs.settings.domain.UpdateUserProfileUseCase
import com.ivangarzab.kluvs.settings.presentation.SettingsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val settingsFeatureModule = module {
    factoryOf(::GetEditableProfileUseCase)
    factoryOf(::UpdateUserProfileUseCase)
    factoryOf(::SettingsViewModel)
}
