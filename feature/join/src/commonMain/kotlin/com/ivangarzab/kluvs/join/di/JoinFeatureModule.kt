package com.ivangarzab.kluvs.join.di

import com.ivangarzab.kluvs.join.domain.JoinClubUseCase
import com.ivangarzab.kluvs.join.domain.PreviewInviteUseCase
import com.ivangarzab.kluvs.join.presentation.JoinViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val joinFeatureModule = module {
    // Use Cases
    factoryOf(::PreviewInviteUseCase)
    factoryOf(::JoinClubUseCase)

    // ViewModels
    factoryOf(::JoinViewModel)
}
