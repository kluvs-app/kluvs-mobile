package com.ivangarzab.kluvs.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.ivangarzab.kluvs.database.dao.BookDao
import com.ivangarzab.kluvs.database.dao.ClubDao
import com.ivangarzab.kluvs.database.dao.DiscussionAttendanceDao
import com.ivangarzab.kluvs.database.dao.DiscussionDao
import com.ivangarzab.kluvs.database.dao.DiscussionNoteDao
import com.ivangarzab.kluvs.database.dao.LikeDao
import com.ivangarzab.kluvs.database.dao.MemberDao
import com.ivangarzab.kluvs.database.dao.ProgressDao
import com.ivangarzab.kluvs.database.dao.ServerDao
import com.ivangarzab.kluvs.database.dao.SessionDao
import com.ivangarzab.kluvs.database.dao.ShelfDao
import com.ivangarzab.kluvs.database.entities.BookEntity
import com.ivangarzab.kluvs.database.entities.ClubEntity
import com.ivangarzab.kluvs.database.entities.ClubMemberCrossRef
import com.ivangarzab.kluvs.database.entities.DiscussionAttendanceEntity
import com.ivangarzab.kluvs.database.entities.DiscussionEntity
import com.ivangarzab.kluvs.database.entities.DiscussionNoteEntity
import com.ivangarzab.kluvs.database.entities.LikeEntity
import com.ivangarzab.kluvs.database.entities.MemberEntity
import com.ivangarzab.kluvs.database.entities.ProgressEntity
import com.ivangarzab.kluvs.database.entities.ServerEntity
import com.ivangarzab.kluvs.database.entities.SessionEntity
import com.ivangarzab.kluvs.database.entities.SessionMemberEntity
import com.ivangarzab.kluvs.database.entities.ShelfEntity

/**
 * Room Database interface for Kluvs app.
 */
interface KluvsDatabase {
    fun clubDao(): ClubDao
    fun memberDao(): MemberDao
    fun sessionDao(): SessionDao
    fun bookDao(): BookDao
    fun discussionDao(): DiscussionDao
    fun serverDao(): ServerDao
    fun shelfDao(): ShelfDao
    fun likeDao(): LikeDao
    fun progressDao(): ProgressDao
    fun discussionNoteDao(): DiscussionNoteDao
    fun discussionAttendanceDao(): DiscussionAttendanceDao
}

/**
 * Room Database implementation for Kluvs app.
 *
 * Stores cached data for servers, clubs, members, sessions, books, shelf
 * entries, likes, reading progress, discussion notes, and discussion attendance.
 */
@Database(
    entities = [
        ServerEntity::class,
        ClubEntity::class,
        MemberEntity::class,
        ClubMemberCrossRef::class,
        SessionEntity::class,
        SessionMemberEntity::class,
        BookEntity::class,
        DiscussionEntity::class,
        ShelfEntity::class,
        LikeEntity::class,
        ProgressEntity::class,
        DiscussionNoteEntity::class,
        DiscussionAttendanceEntity::class
    ],
    version = 5,
    exportSchema = true
)
@ConstructedBy(KluvsDatabaseConstructor::class)
abstract class KluvsDatabaseImpl : RoomDatabase(), KluvsDatabase {
    abstract override fun serverDao(): ServerDao
    abstract override fun clubDao(): ClubDao
    abstract override fun memberDao(): MemberDao
    abstract override fun sessionDao(): SessionDao
    abstract override fun bookDao(): BookDao
    abstract override fun discussionDao(): DiscussionDao
    abstract override fun shelfDao(): ShelfDao
    abstract override fun likeDao(): LikeDao
    abstract override fun progressDao(): ProgressDao
    abstract override fun discussionNoteDao(): DiscussionNoteDao
    abstract override fun discussionAttendanceDao(): DiscussionAttendanceDao
}

/**
 * Platform-agnostic database constructor.
 *
 * The Room compiler generates the `actual` implementations of when this interface implements
 * the [RoomDatabaseConstructor] interface.
 * @see https://developer.android.com/kotlin/multiplatform/room#defining-database
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING", "KotlinNoActualForExpect")
expect object KluvsDatabaseConstructor : RoomDatabaseConstructor<KluvsDatabaseImpl> {
    override fun initialize(): KluvsDatabaseImpl
}
