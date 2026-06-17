package com.ivangarzab.kluvs.clubs.di

import com.ivangarzab.kluvs.clubs.domain.CreateDiscussionUseCase
import com.ivangarzab.kluvs.clubs.domain.CreateSessionUseCase
import com.ivangarzab.kluvs.clubs.domain.DeleteClubUseCase
import com.ivangarzab.kluvs.clubs.domain.DeleteDiscussionUseCase
import com.ivangarzab.kluvs.clubs.domain.DeleteSessionUseCase
import com.ivangarzab.kluvs.clubs.domain.GetActiveSessionUseCase
import com.ivangarzab.kluvs.clubs.domain.GetClubDetailsUseCase
import com.ivangarzab.kluvs.clubs.domain.GetClubMembersUseCase
import com.ivangarzab.kluvs.clubs.domain.GetMemberClubsUseCase
import com.ivangarzab.kluvs.clubs.domain.RemoveMemberUseCase
import com.ivangarzab.kluvs.clubs.domain.SearchBooksUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateClubUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateDiscussionUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateMemberRoleUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateSessionUseCase
import com.ivangarzab.kluvs.clubs.presentation.ClubDetailsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val clubsFeatureModule = module {
    // Use Cases — read
    factoryOf(::GetActiveSessionUseCase)
    factoryOf(::GetClubDetailsUseCase)
    factoryOf(::GetClubMembersUseCase)
    factoryOf(::GetMemberClubsUseCase)
    factoryOf(::SearchBooksUseCase)

    // Use Cases — write (admin operations)
    factoryOf(::UpdateClubUseCase)
    factoryOf(::DeleteClubUseCase)
    factoryOf(::CreateSessionUseCase)
    factoryOf(::UpdateSessionUseCase)
    factoryOf(::DeleteSessionUseCase)
    factoryOf(::CreateDiscussionUseCase)
    factoryOf(::UpdateDiscussionUseCase)
    factoryOf(::DeleteDiscussionUseCase)
    factoryOf(::UpdateMemberRoleUseCase)
    factoryOf(::RemoveMemberUseCase)

    // ViewModels
    factoryOf(::ClubDetailsViewModel)
}
