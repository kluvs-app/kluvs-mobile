package com.ivangarzab.kluvs.data.di

import com.ivangarzab.kluvs.data.local.cache.CachePolicy
import com.ivangarzab.kluvs.data.local.source.BookLocalDataSource
import com.ivangarzab.kluvs.data.local.source.BookLocalDataSourceImpl
import com.ivangarzab.kluvs.data.local.source.ClubLocalDataSource
import com.ivangarzab.kluvs.data.local.source.ClubLocalDataSourceImpl
import com.ivangarzab.kluvs.data.local.source.DiscussionAttendanceLocalDataSource
import com.ivangarzab.kluvs.data.local.source.DiscussionAttendanceLocalDataSourceImpl
import com.ivangarzab.kluvs.data.local.source.DiscussionNoteLocalDataSource
import com.ivangarzab.kluvs.data.local.source.DiscussionNoteLocalDataSourceImpl
import com.ivangarzab.kluvs.data.local.source.LikeLocalDataSource
import com.ivangarzab.kluvs.data.local.source.LikeLocalDataSourceImpl
import com.ivangarzab.kluvs.data.local.source.MemberLocalDataSource
import com.ivangarzab.kluvs.data.local.source.MemberLocalDataSourceImpl
import com.ivangarzab.kluvs.data.local.source.ProgressLocalDataSource
import com.ivangarzab.kluvs.data.local.source.ProgressLocalDataSourceImpl
import com.ivangarzab.kluvs.data.local.source.ServerLocalDataSource
import com.ivangarzab.kluvs.data.local.source.ServerLocalDataSourceImpl
import com.ivangarzab.kluvs.data.local.source.SessionLocalDataSource
import com.ivangarzab.kluvs.data.local.source.SessionLocalDataSourceImpl
import com.ivangarzab.kluvs.data.local.source.ShelfLocalDataSource
import com.ivangarzab.kluvs.data.local.source.ShelfLocalDataSourceImpl
import com.ivangarzab.kluvs.data.remote.api.AvatarService
import com.ivangarzab.kluvs.data.remote.api.AvatarServiceImpl
import com.ivangarzab.kluvs.data.remote.api.BookEnrichmentService
import com.ivangarzab.kluvs.data.remote.api.BookEnrichmentServiceImpl
import com.ivangarzab.kluvs.data.remote.api.BookService
import com.ivangarzab.kluvs.data.remote.api.BookServiceImpl
import com.ivangarzab.kluvs.data.remote.api.ClubService
import com.ivangarzab.kluvs.data.remote.api.ClubServiceImpl
import com.ivangarzab.kluvs.data.remote.api.DiscussionAttendanceService
import com.ivangarzab.kluvs.data.remote.api.DiscussionAttendanceServiceImpl
import com.ivangarzab.kluvs.data.remote.api.DiscussionNoteService
import com.ivangarzab.kluvs.data.remote.api.DiscussionNoteServiceImpl
import com.ivangarzab.kluvs.data.remote.api.DiscussionService
import com.ivangarzab.kluvs.data.remote.api.DiscussionServiceImpl
import com.ivangarzab.kluvs.data.remote.api.JoinService
import com.ivangarzab.kluvs.data.remote.api.JoinServiceImpl
import com.ivangarzab.kluvs.data.remote.api.LikeService
import com.ivangarzab.kluvs.data.remote.api.LikeServiceImpl
import com.ivangarzab.kluvs.data.remote.api.MemberService
import com.ivangarzab.kluvs.data.remote.api.MemberServiceImpl
import com.ivangarzab.kluvs.data.remote.api.ProgressService
import com.ivangarzab.kluvs.data.remote.api.ProgressServiceImpl
import com.ivangarzab.kluvs.data.remote.api.ShelfService
import com.ivangarzab.kluvs.data.remote.api.ShelfServiceImpl
import com.ivangarzab.kluvs.data.remote.api.ServerService
import com.ivangarzab.kluvs.data.remote.api.ServerServiceImpl
import com.ivangarzab.kluvs.data.remote.api.SessionService
import com.ivangarzab.kluvs.data.remote.api.SessionServiceImpl
import com.ivangarzab.kluvs.data.remote.source.AvatarRemoteDataSource
import com.ivangarzab.kluvs.data.remote.source.AvatarRemoteDataSourceImpl
import com.ivangarzab.kluvs.data.remote.source.BookEnrichmentRemoteDataSource
import com.ivangarzab.kluvs.data.remote.source.BookEnrichmentRemoteDataSourceImpl
import com.ivangarzab.kluvs.data.remote.source.BookRemoteDataSource
import com.ivangarzab.kluvs.data.remote.source.BookRemoteDataSourceImpl
import com.ivangarzab.kluvs.data.remote.source.ClubRemoteDataSource
import com.ivangarzab.kluvs.data.remote.source.ClubRemoteDataSourceImpl
import com.ivangarzab.kluvs.data.remote.source.DiscussionAttendanceRemoteDataSource
import com.ivangarzab.kluvs.data.remote.source.DiscussionAttendanceRemoteDataSourceImpl
import com.ivangarzab.kluvs.data.remote.source.DiscussionNoteRemoteDataSource
import com.ivangarzab.kluvs.data.remote.source.DiscussionNoteRemoteDataSourceImpl
import com.ivangarzab.kluvs.data.remote.source.DiscussionRemoteDataSource
import com.ivangarzab.kluvs.data.remote.source.DiscussionRemoteDataSourceImpl
import com.ivangarzab.kluvs.data.remote.source.JoinRemoteDataSource
import com.ivangarzab.kluvs.data.remote.source.JoinRemoteDataSourceImpl
import com.ivangarzab.kluvs.data.remote.source.LikeRemoteDataSource
import com.ivangarzab.kluvs.data.remote.source.LikeRemoteDataSourceImpl
import com.ivangarzab.kluvs.data.remote.source.MemberRemoteDataSource
import com.ivangarzab.kluvs.data.remote.source.MemberRemoteDataSourceImpl
import com.ivangarzab.kluvs.data.remote.source.ProgressRemoteDataSource
import com.ivangarzab.kluvs.data.remote.source.ProgressRemoteDataSourceImpl
import com.ivangarzab.kluvs.data.remote.source.ServerRemoteDataSource
import com.ivangarzab.kluvs.data.remote.source.ServerRemoteDataSourceImpl
import com.ivangarzab.kluvs.data.remote.source.SessionRemoteDataSource
import com.ivangarzab.kluvs.data.remote.source.SessionRemoteDataSourceImpl
import com.ivangarzab.kluvs.data.remote.source.ShelfRemoteDataSource
import com.ivangarzab.kluvs.data.remote.source.ShelfRemoteDataSourceImpl
import com.ivangarzab.kluvs.data.repositories.AvatarRepository
import com.ivangarzab.kluvs.data.repositories.AvatarRepositoryImpl
import com.ivangarzab.kluvs.data.repositories.BookEnrichmentRepository
import com.ivangarzab.kluvs.data.repositories.BookEnrichmentRepositoryImpl
import com.ivangarzab.kluvs.data.repositories.BookRepository
import com.ivangarzab.kluvs.data.repositories.BookRepositoryImpl
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.data.repositories.ClubRepositoryImpl
import com.ivangarzab.kluvs.data.repositories.DiscussionAttendanceRepository
import com.ivangarzab.kluvs.data.repositories.DiscussionAttendanceRepositoryImpl
import com.ivangarzab.kluvs.data.repositories.DiscussionNoteRepository
import com.ivangarzab.kluvs.data.repositories.DiscussionNoteRepositoryImpl
import com.ivangarzab.kluvs.data.repositories.DiscussionRepository
import com.ivangarzab.kluvs.data.repositories.DiscussionRepositoryImpl
import com.ivangarzab.kluvs.data.repositories.JoinRepository
import com.ivangarzab.kluvs.data.repositories.JoinRepositoryImpl
import com.ivangarzab.kluvs.data.repositories.LikeRepository
import com.ivangarzab.kluvs.data.repositories.LikeRepositoryImpl
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepositoryImpl
import com.ivangarzab.kluvs.data.repositories.ProgressRepository
import com.ivangarzab.kluvs.data.repositories.ProgressRepositoryImpl
import com.ivangarzab.kluvs.data.repositories.ServerRepository
import com.ivangarzab.kluvs.data.repositories.ServerRepositoryImpl
import com.ivangarzab.kluvs.data.repositories.SessionRepository
import com.ivangarzab.kluvs.data.repositories.SessionRepositoryImpl
import com.ivangarzab.kluvs.data.repositories.ShelfRepository
import com.ivangarzab.kluvs.data.repositories.ShelfRepositoryImpl
import com.ivangarzab.kluvs.database.KluvsDatabase
import io.github.jan.supabase.SupabaseClient
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
val coreDataModule = module {
    // Database - platform-specific module
    includes(databaseModule)

    // Cache Policy
    single<CachePolicy> { CachePolicy() }

    // Local Data Sources
    single<ClubLocalDataSource> { ClubLocalDataSourceImpl(get<KluvsDatabase>()) }
    single<ServerLocalDataSource> { ServerLocalDataSourceImpl(get<KluvsDatabase>()) }
    single<MemberLocalDataSource> { MemberLocalDataSourceImpl(get<KluvsDatabase>()) }
    single<SessionLocalDataSource> { SessionLocalDataSourceImpl(get<KluvsDatabase>()) }
    single<BookLocalDataSource> { BookLocalDataSourceImpl(get<KluvsDatabase>()) }
    single<ShelfLocalDataSource> { ShelfLocalDataSourceImpl(get<KluvsDatabase>()) }
    single<LikeLocalDataSource> { LikeLocalDataSourceImpl(get<KluvsDatabase>()) }
    single<ProgressLocalDataSource> { ProgressLocalDataSourceImpl(get<KluvsDatabase>()) }
    single<DiscussionNoteLocalDataSource> { DiscussionNoteLocalDataSourceImpl(get<KluvsDatabase>()) }
    single<DiscussionAttendanceLocalDataSource> { DiscussionAttendanceLocalDataSourceImpl(get<KluvsDatabase>()) }

    // Services
    single<BookService> { BookServiceImpl(get<SupabaseClient>()) }
    single<BookEnrichmentService> { BookEnrichmentServiceImpl(get<SupabaseClient>()) }
    single<ClubService> { ClubServiceImpl(get<SupabaseClient>()) }
    single<MemberService> { MemberServiceImpl(get<SupabaseClient>()) }
    single<ServerService> { ServerServiceImpl(get<SupabaseClient>()) }
    single<SessionService> { SessionServiceImpl(get<SupabaseClient>()) }
    single<AvatarService> { AvatarServiceImpl(get<SupabaseClient>()) }
    single<ShelfService> { ShelfServiceImpl(get<SupabaseClient>()) }
    single<LikeService> { LikeServiceImpl(get<SupabaseClient>()) }
    single<ProgressService> { ProgressServiceImpl(get<SupabaseClient>()) }
    single<DiscussionService> { DiscussionServiceImpl(get<SupabaseClient>()) }
    single<DiscussionNoteService> { DiscussionNoteServiceImpl(get<SupabaseClient>()) }
    single<DiscussionAttendanceService> { DiscussionAttendanceServiceImpl(get<SupabaseClient>()) }
    single<JoinService> { JoinServiceImpl(get<SupabaseClient>()) }

    // Remote Data Sources
    single<AvatarRemoteDataSource> { AvatarRemoteDataSourceImpl(get<AvatarService>()) }
    single<BookRemoteDataSource> { BookRemoteDataSourceImpl(get<BookService>()) }
    single<BookEnrichmentRemoteDataSource> { BookEnrichmentRemoteDataSourceImpl(get<BookEnrichmentService>()) }
    single<ClubRemoteDataSource> { ClubRemoteDataSourceImpl(get<ClubService>()) }
    single<MemberRemoteDataSource> { MemberRemoteDataSourceImpl(get<MemberService>()) }
    single<ServerRemoteDataSource> { ServerRemoteDataSourceImpl(get<ServerService>()) }
    single<SessionRemoteDataSource> { SessionRemoteDataSourceImpl(get<SessionService>()) }
    single<ShelfRemoteDataSource> { ShelfRemoteDataSourceImpl(get<ShelfService>()) }
    single<LikeRemoteDataSource> { LikeRemoteDataSourceImpl(get<LikeService>()) }
    single<ProgressRemoteDataSource> { ProgressRemoteDataSourceImpl(get<ProgressService>()) }
    single<DiscussionRemoteDataSource> { DiscussionRemoteDataSourceImpl(get<DiscussionService>()) }
    single<DiscussionNoteRemoteDataSource> { DiscussionNoteRemoteDataSourceImpl(get<DiscussionNoteService>()) }
    single<DiscussionAttendanceRemoteDataSource> { DiscussionAttendanceRemoteDataSourceImpl(get<DiscussionAttendanceService>()) }
    single<JoinRemoteDataSource> { JoinRemoteDataSourceImpl(get<JoinService>()) }

    // Repositories
    single<AvatarRepository> { AvatarRepositoryImpl(get<AvatarRemoteDataSource>()) }
    single<BookRepository> {
        BookRepositoryImpl(
            get<BookRemoteDataSource>(),
            get<BookLocalDataSource>()
        )
    }
    single<BookEnrichmentRepository> { BookEnrichmentRepositoryImpl(get<BookEnrichmentRemoteDataSource>()) }
    single<ServerRepository> {
        ServerRepositoryImpl(
            get<ServerRemoteDataSource>(),
            get<ServerLocalDataSource>(),
            get<CachePolicy>()
        )
    }
    single<ClubRepository> {
        ClubRepositoryImpl(
            get<ClubRemoteDataSource>(),
            get<ClubLocalDataSource>(),
            get<CachePolicy>()
        )
    }
    single<MemberRepository> {
        MemberRepositoryImpl(
            get<MemberRemoteDataSource>(),
            get<MemberLocalDataSource>(),
            get<CachePolicy>()
        )
    }
    single<SessionRepository> {
        SessionRepositoryImpl(
            get<SessionRemoteDataSource>(),
            get<SessionLocalDataSource>(),
            get<CachePolicy>()
        )
    }
    single<ShelfRepository> {
        ShelfRepositoryImpl(
            get<ShelfRemoteDataSource>(),
            get<ShelfLocalDataSource>(),
            get<CachePolicy>()
        )
    }
    single<LikeRepository> {
        LikeRepositoryImpl(
            get<LikeRemoteDataSource>(),
            get<LikeLocalDataSource>(),
            get<CachePolicy>()
        )
    }
    single<ProgressRepository> {
        ProgressRepositoryImpl(
            get<ProgressRemoteDataSource>(),
            get<ProgressLocalDataSource>(),
            get<CachePolicy>()
        )
    }
    single<DiscussionRepository> { DiscussionRepositoryImpl(get<DiscussionRemoteDataSource>()) }
    single<DiscussionNoteRepository> {
        DiscussionNoteRepositoryImpl(
            get<DiscussionNoteRemoteDataSource>(),
            get<DiscussionNoteLocalDataSource>(),
            get<CachePolicy>()
        )
    }
    single<DiscussionAttendanceRepository> {
        DiscussionAttendanceRepositoryImpl(
            get<DiscussionAttendanceRemoteDataSource>(),
            get<DiscussionAttendanceLocalDataSource>()
        )
    }
    single<JoinRepository> { JoinRepositoryImpl(get<JoinRemoteDataSource>()) }
}

expect val databaseModule: Module