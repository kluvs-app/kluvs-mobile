package com.ivangarzab.kluvs.clubs.di

import com.ivangarzab.kluvs.clubs.domain.ClearAttendanceUseCase
import com.ivangarzab.kluvs.clubs.domain.CreateClubUseCase
import com.ivangarzab.kluvs.clubs.domain.CreateDiscussionNoteUseCase
import com.ivangarzab.kluvs.clubs.domain.CreateDiscussionUseCase
import com.ivangarzab.kluvs.clubs.domain.CreateSessionUseCase
import com.ivangarzab.kluvs.clubs.domain.DeleteClubUseCase
import com.ivangarzab.kluvs.clubs.domain.DeleteDiscussionNoteUseCase
import com.ivangarzab.kluvs.clubs.domain.DeleteDiscussionUseCase
import com.ivangarzab.kluvs.clubs.domain.DeleteSessionUseCase
import com.ivangarzab.kluvs.clubs.domain.FinishSessionUseCase
import com.ivangarzab.kluvs.clubs.domain.GetActiveSessionUseCase
import com.ivangarzab.kluvs.clubs.domain.GetAttendanceRosterUseCase
import com.ivangarzab.kluvs.clubs.domain.GetClubDetailsUseCase
import com.ivangarzab.kluvs.clubs.domain.GetClubMembersUseCase
import com.ivangarzab.kluvs.clubs.domain.GetDiscussionNoteUseCase
import com.ivangarzab.kluvs.clubs.domain.GetMemberClubsUseCase
import com.ivangarzab.kluvs.clubs.domain.RemoveMemberUseCase
import com.ivangarzab.kluvs.clubs.domain.RotateInviteLinkUseCase
import com.ivangarzab.kluvs.clubs.domain.SearchBooksUseCase
import com.ivangarzab.kluvs.clubs.domain.SetAttendanceUseCase
import com.ivangarzab.kluvs.clubs.domain.ToggleSessionParticipationUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateClubUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateDiscussionNoteUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateDiscussionUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateJoinPolicyUseCase
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
    factoryOf(::GetAttendanceRosterUseCase)
    factoryOf(::GetDiscussionNoteUseCase)

    // Use Cases — write (self-serve)
    factoryOf(::SetAttendanceUseCase)
    factoryOf(::ClearAttendanceUseCase)
    factoryOf(::CreateDiscussionNoteUseCase)
    factoryOf(::UpdateDiscussionNoteUseCase)
    factoryOf(::DeleteDiscussionNoteUseCase)

    // Use Cases — write (admin operations)
    factoryOf(::CreateClubUseCase)
    factoryOf(::UpdateClubUseCase)
    factoryOf(::UpdateJoinPolicyUseCase)
    factoryOf(::RotateInviteLinkUseCase)
    factoryOf(::DeleteClubUseCase)
    factoryOf(::CreateSessionUseCase)
    factoryOf(::UpdateSessionUseCase)
    factoryOf(::DeleteSessionUseCase)
    factoryOf(::CreateDiscussionUseCase)
    factoryOf(::UpdateDiscussionUseCase)
    factoryOf(::DeleteDiscussionUseCase)
    factoryOf(::UpdateMemberRoleUseCase)
    factoryOf(::RemoveMemberUseCase)
    factoryOf(::FinishSessionUseCase)
    factoryOf(::ToggleSessionParticipationUseCase)

    // ViewModels
    // ClubDetailsViewModel now exceeds factoryOf's 22-type-param ceiling, so it's
    // wired explicitly instead — Koin resolves each `get()` by type as usual.
    factory {
        ClubDetailsViewModel(
            getClubDetails = get(),
            getActiveSession = get(),
            getClubMembers = get(),
            getMemberClubsUseCase = get(),
            createClubUseCase = get(),
            updateClubUseCase = get(),
            updateJoinPolicyUseCase = get(),
            rotateInviteLinkUseCase = get(),
            deleteClubUseCase = get(),
            createSessionUseCase = get(),
            updateSessionUseCase = get(),
            deleteSessionUseCase = get(),
            createDiscussionUseCase = get(),
            updateDiscussionUseCase = get(),
            deleteDiscussionUseCase = get(),
            updateMemberRoleUseCase = get(),
            removeMemberUseCase = get(),
            getSessionProgressUseCase = get(),
            saveProgressUseCase = get(),
            finishSessionUseCase = get(),
            toggleSessionParticipationUseCase = get(),
            getAttendanceRosterUseCase = get(),
            setAttendanceUseCase = get(),
            clearAttendanceUseCase = get(),
            getDiscussionNoteUseCase = get(),
            createDiscussionNoteUseCase = get(),
            updateDiscussionNoteUseCase = get(),
            deleteDiscussionNoteUseCase = get()
        )
    }
}
