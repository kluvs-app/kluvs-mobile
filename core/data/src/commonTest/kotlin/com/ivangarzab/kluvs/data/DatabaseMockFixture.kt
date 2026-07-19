package com.ivangarzab.kluvs.data

import com.ivangarzab.kluvs.database.KluvsDatabase
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
import com.ivangarzab.kluvs.database.entities.DiscussionEntity
import com.ivangarzab.kluvs.database.entities.MemberEntity
import com.ivangarzab.kluvs.database.entities.ServerEntity
import com.ivangarzab.kluvs.database.entities.SessionEntity
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock

/**
 * Test fixture for mocking a KluvsDatabase instance with all DAOs.
 *
 * Provides pre-configured mock DAOs that satisfy the database interface.
 * All DAO methods are mocked with default behaviors (returning Unit or empty lists).
 *
 * Usage:
 * ```
 * val fixture = DatabaseMockFixture()
 * val dataSource = ClubLocalDataSourceImpl(fixture.database)
 * // Use fixture.clubDao, fixture.memberDao, etc. to override specific behaviors
 * ```
 */
class DatabaseMockFixture {
    val clubDao: ClubDao = mock<ClubDao>()
    val memberDao: MemberDao = mock<MemberDao>()
    val sessionDao: SessionDao = mock<SessionDao>()
    val bookDao: BookDao = mock<BookDao>()
    val discussionDao: DiscussionDao = mock<DiscussionDao>()
    val serverDao: ServerDao = mock<ServerDao>()
    val shelfDao: ShelfDao = mock<ShelfDao>()
    val likeDao: LikeDao = mock<LikeDao>()
    val progressDao: ProgressDao = mock<ProgressDao>()
    val discussionNoteDao: DiscussionNoteDao = mock<DiscussionNoteDao>()
    val discussionAttendanceDao: DiscussionAttendanceDao = mock<DiscussionAttendanceDao>()

    val database: KluvsDatabase = mock<KluvsDatabase>().also { db ->
        every { db.clubDao() } returns clubDao
        every { db.memberDao() } returns memberDao
        every { db.sessionDao() } returns sessionDao
        every { db.bookDao() } returns bookDao
        every { db.discussionDao() } returns discussionDao
        every { db.serverDao() } returns serverDao
        every { db.shelfDao() } returns shelfDao
        every { db.likeDao() } returns likeDao
        every { db.progressDao() } returns progressDao
        every { db.discussionNoteDao() } returns discussionNoteDao
        every { db.discussionAttendanceDao() } returns discussionAttendanceDao

        // Mock all insert/update operations to accept any argument and return Unit
        everySuspend { clubDao.insertClub(any()) } returns Unit
        everySuspend { clubDao.insertClubs(any()) } returns Unit
        everySuspend { clubDao.deleteClub(any()) } returns Unit
        everySuspend { clubDao.deleteAll() } returns Unit

        everySuspend { memberDao.insertMember(any()) } returns Unit
        everySuspend { memberDao.insertMembers(any()) } returns Unit
        everySuspend { memberDao.insertClubMemberCrossRef(any()) } returns Unit
        everySuspend { memberDao.deleteMember(any()) } returns Unit
        everySuspend { memberDao.deleteClubMemberCrossRef(any(), any()) } returns Unit
        everySuspend { memberDao.deleteAll() } returns Unit
        everySuspend { memberDao.deleteAllCrossRefs() } returns Unit

        everySuspend { sessionDao.insertSession(any()) } returns Unit
        everySuspend { sessionDao.insertSessions(any()) } returns Unit
        everySuspend { sessionDao.deleteSession(any()) } returns Unit
        everySuspend { sessionDao.deleteAll() } returns Unit
        everySuspend { sessionDao.insertSessionMembers(any()) } returns Unit
        everySuspend { sessionDao.deleteSessionMembers(any()) } returns Unit
        everySuspend { sessionDao.deleteAllSessionMembers() } returns Unit

        everySuspend { bookDao.insertBook(any()) } returns Unit
        everySuspend { bookDao.insertBooks(any()) } returns Unit
        everySuspend { bookDao.deleteBook(any()) } returns Unit
        everySuspend { bookDao.deleteAll() } returns Unit

        everySuspend { discussionDao.insertDiscussion(any()) } returns Unit
        everySuspend { discussionDao.deleteAll() } returns Unit

        everySuspend { serverDao.insertServer(any()) } returns Unit
        everySuspend { serverDao.deleteServer(any()) } returns Unit
        everySuspend { serverDao.deleteAll() } returns Unit

        everySuspend { shelfDao.insertShelfEntry(any()) } returns Unit
        everySuspend { shelfDao.insertShelfEntries(any()) } returns Unit
        everySuspend { shelfDao.deleteShelfEntry(any()) } returns Unit
        everySuspend { shelfDao.deleteAll() } returns Unit

        everySuspend { likeDao.insertLike(any()) } returns Unit
        everySuspend { likeDao.deleteLike(any()) } returns Unit
        everySuspend { likeDao.deleteAll() } returns Unit

        everySuspend { progressDao.insertProgress(any()) } returns Unit
        everySuspend { progressDao.insertProgressEntries(any()) } returns Unit
        everySuspend { progressDao.deleteProgress(any()) } returns Unit
        everySuspend { progressDao.deleteAll() } returns Unit

        everySuspend { discussionNoteDao.insertNote(any()) } returns Unit
        everySuspend { discussionNoteDao.deleteNote(any()) } returns Unit
        everySuspend { discussionNoteDao.deleteAll() } returns Unit

        everySuspend { discussionAttendanceDao.insertAttendance(any()) } returns Unit
        everySuspend { discussionAttendanceDao.deleteAttendance(any()) } returns Unit
        everySuspend { discussionAttendanceDao.deleteAll() } returns Unit

        // Mock read operations to return empty lists by default
        everySuspend { clubDao.getClub(any()) } returns null
        everySuspend { clubDao.getClubsForServer(any()) } returns emptyList()
        everySuspend { memberDao.getMember(any()) } returns null
        everySuspend { memberDao.getMemberByUserId(any()) } returns null
        everySuspend { memberDao.getMembersForClub(any()) } returns emptyList()
        everySuspend { memberDao.getClubsForMember(any()) } returns emptyList()
        everySuspend { sessionDao.getSession(any()) } returns null
        everySuspend { sessionDao.getSessionsForClub(any()) } returns emptyList()
        everySuspend { sessionDao.getSessionMembers(any()) } returns emptyList()
        everySuspend { bookDao.getBook(any()) } returns null
        everySuspend { bookDao.getAllBooks() } returns emptyList()
        everySuspend { serverDao.getServer(any()) } returns null
        everySuspend { serverDao.getAllServers() } returns emptyList()
        everySuspend { discussionDao.getDiscussionsForSession(any()) } returns emptyList()

        everySuspend { shelfDao.getShelfEntry(any()) } returns null
        everySuspend { shelfDao.getShelf() } returns emptyList()
        everySuspend { shelfDao.getLastFetchedAt(any()) } returns null
        everySuspend { likeDao.getLike(any()) } returns null
        everySuspend { likeDao.getLastFetchedAt(any()) } returns null
        everySuspend { progressDao.getProgress(any()) } returns null
        everySuspend { progressDao.getProgressEntries(any(), any(), any()) } returns emptyList()
        everySuspend { progressDao.getLastFetchedAt(any()) } returns null
        everySuspend { discussionNoteDao.getNote(any()) } returns null
        everySuspend { discussionNoteDao.getNoteById(any()) } returns null
        everySuspend { discussionNoteDao.getLastFetchedAt(any()) } returns null
        everySuspend { discussionAttendanceDao.getAttendance(any()) } returns null
        everySuspend { discussionAttendanceDao.getLastFetchedAt(any()) } returns null
    }
}