package com.ivangarzab.kluvs.data.di

import com.ivangarzab.kluvs.data.local.cache.CachePolicy
import com.ivangarzab.kluvs.data.local.source.BookLocalDataSource
import com.ivangarzab.kluvs.data.local.source.BookLocalDataSourceImpl
import com.ivangarzab.kluvs.data.local.source.ClubLocalDataSource
import com.ivangarzab.kluvs.data.local.source.ClubLocalDataSourceImpl
import com.ivangarzab.kluvs.data.local.source.MemberLocalDataSource
import com.ivangarzab.kluvs.data.local.source.MemberLocalDataSourceImpl
import com.ivangarzab.kluvs.data.local.source.ServerLocalDataSource
import com.ivangarzab.kluvs.data.local.source.ServerLocalDataSourceImpl
import com.ivangarzab.kluvs.data.local.source.SessionLocalDataSource
import com.ivangarzab.kluvs.data.local.source.SessionLocalDataSourceImpl
import com.ivangarzab.kluvs.data.remote.api.AvatarService
import com.ivangarzab.kluvs.data.remote.api.AvatarServiceImpl
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
import com.ivangarzab.kluvs.data.remote.source.BookRemoteDataSource
import com.ivangarzab.kluvs.data.remote.source.BookRemoteDataSourceImpl
import com.ivangarzab.kluvs.data.remote.source.ClubRemoteDataSource
import com.ivangarzab.kluvs.data.remote.source.ClubRemoteDataSourceImpl
import com.ivangarzab.kluvs.data.remote.source.MemberRemoteDataSource
import com.ivangarzab.kluvs.data.remote.source.MemberRemoteDataSourceImpl
import com.ivangarzab.kluvs.data.remote.source.ServerRemoteDataSource
import com.ivangarzab.kluvs.data.remote.source.ServerRemoteDataSourceImpl
import com.ivangarzab.kluvs.data.remote.source.SessionRemoteDataSource
import com.ivangarzab.kluvs.data.remote.source.SessionRemoteDataSourceImpl
import com.ivangarzab.kluvs.data.repositories.AvatarRepository
import com.ivangarzab.kluvs.data.repositories.AvatarRepositoryImpl
import com.ivangarzab.kluvs.data.repositories.BookRepository
import com.ivangarzab.kluvs.data.repositories.BookRepositoryImpl
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.data.repositories.ClubRepositoryImpl
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepositoryImpl
import com.ivangarzab.kluvs.data.repositories.ServerRepository
import com.ivangarzab.kluvs.data.repositories.ServerRepositoryImpl
import com.ivangarzab.kluvs.data.repositories.SessionRepository
import com.ivangarzab.kluvs.data.repositories.SessionRepositoryImpl
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

    // Services
    single<BookService> { BookServiceImpl(get<SupabaseClient>()) }
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
    single<ClubRemoteDataSource> { ClubRemoteDataSourceImpl(get<ClubService>()) }
    single<MemberRemoteDataSource> { MemberRemoteDataSourceImpl(get<MemberService>()) }
    single<ServerRemoteDataSource> { ServerRemoteDataSourceImpl(get<ServerService>()) }
    single<SessionRemoteDataSource> { SessionRemoteDataSourceImpl(get<SessionService>()) }

    // Repositories
    single<AvatarRepository> { AvatarRepositoryImpl(get<AvatarRemoteDataSource>()) }
    single<BookRepository> {
        BookRepositoryImpl(
            get<BookRemoteDataSource>(),
            get<BookLocalDataSource>()
        )
    }
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
}

expect val databaseModule: Module