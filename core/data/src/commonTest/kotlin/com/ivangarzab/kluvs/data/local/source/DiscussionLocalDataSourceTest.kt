package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.kluvs.data.DatabaseMockFixture
import com.ivangarzab.kluvs.database.entities.DiscussionEntity
import com.ivangarzab.kluvs.database.entities.SessionEntity
import com.ivangarzab.kluvs.model.Discussion
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matching
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiscussionLocalDataSourceTest {

    private lateinit var fixture: DatabaseMockFixture
    private lateinit var dataSource: DiscussionLocalDataSource

    private fun setup() {
        fixture = DatabaseMockFixture()
        dataSource = DiscussionLocalDataSourceImpl(fixture.database)
    }

    @Test
    fun `deleteDiscussion removes the cached row`() = runTest {
        setup()

        dataSource.deleteDiscussion("disc-1")

        verifySuspend { fixture.discussionDao.deleteDiscussionById("disc-1") }
    }

    @Test
    fun `markClubStaleForSession marks the owning club stale`() = runTest {
        setup()
        everySuspend { fixture.sessionDao.getSession("session-1") } returns SessionEntity("session-1", "club-1", "book-1", null, 0)

        dataSource.markClubStaleForSession("session-1")

        verifySuspend { fixture.clubDao.markStale("club-1") }
    }

    @Test
    fun `markClubStaleForSession does nothing when the session isn't cached`() = runTest {
        setup()

        dataSource.markClubStaleForSession("session-1")

        verifySuspend(VerifyMode.not) { fixture.clubDao.markStale(any()) }
    }

    @Test
    fun `markClubStaleForDiscussion follows discussion to session to club`() = runTest {
        setup()
        everySuspend { fixture.discussionDao.getDiscussion("disc-1") } returns DiscussionEntity("disc-1", "session-1", "Ch. 1", "2026-08-01T18:00", null, 0)
        everySuspend { fixture.sessionDao.getSession("session-1") } returns SessionEntity("session-1", "club-1", "book-1", null, 0)

        dataSource.markClubStaleForDiscussion("disc-1")

        verifySuspend { fixture.clubDao.markStale("club-1") }
    }

    private val discussion = Discussion(
        id = "disc-1",
        sessionId = "session-1",
        title = "Ch. 1",
        date = LocalDateTime.parse("2026-08-01T18:00:00")
    )

    @Test
    fun `upsertDiscussion writes the discussion into its cached session`() = runTest {
        setup()
        everySuspend { fixture.sessionDao.getSession("session-1") } returns SessionEntity("session-1", "club-1", "book-1", null, 0)

        assertTrue(dataSource.upsertDiscussion(discussion))

        verifySuspend { fixture.discussionDao.insertDiscussion(matching { it.id == "disc-1" && it.sessionId == "session-1" }) }
    }

    @Test
    fun `upsertDiscussion takes the session from the cached row when the discussion has none`() = runTest {
        setup()
        everySuspend { fixture.discussionDao.getDiscussion("disc-1") } returns DiscussionEntity("disc-1", "session-1", "Old", "2026-08-01T18:00", null, 0)
        everySuspend { fixture.sessionDao.getSession("session-1") } returns SessionEntity("session-1", "club-1", "book-1", null, 0)

        assertTrue(dataSource.upsertDiscussion(discussion.copy(sessionId = null)))

        verifySuspend { fixture.discussionDao.insertDiscussion(matching { it.sessionId == "session-1" }) }
    }

    @Test
    fun `upsertDiscussion does nothing when the session isn't cached`() = runTest {
        setup()

        assertFalse(dataSource.upsertDiscussion(discussion))

        verifySuspend(VerifyMode.not) { fixture.discussionDao.insertDiscussion(any()) }
    }
}
