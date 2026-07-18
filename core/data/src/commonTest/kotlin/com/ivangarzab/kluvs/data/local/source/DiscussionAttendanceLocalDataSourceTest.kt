package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.kluvs.data.DatabaseMockFixture
import com.ivangarzab.kluvs.database.entities.DiscussionAttendanceEntity
import com.ivangarzab.kluvs.model.AttendanceStatus
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DiscussionAttendanceLocalDataSourceTest {

    private lateinit var fixture: DatabaseMockFixture
    private lateinit var dataSource: DiscussionAttendanceLocalDataSource

    private fun setup() {
        fixture = DatabaseMockFixture()
        dataSource = DiscussionAttendanceLocalDataSourceImpl(fixture.database)
    }

    @Test
    fun `getMyStatus returns cached status`() = runTest {
        setup()
        everySuspend { fixture.discussionAttendanceDao.getAttendance("discussion-1") } returns
            DiscussionAttendanceEntity("discussion-1", "YES", 0)

        assertEquals(AttendanceStatus.YES, dataSource.getMyStatus("discussion-1"))
    }

    @Test
    fun `getMyStatus returns null when not cached`() = runTest {
        setup()
        everySuspend { fixture.discussionAttendanceDao.getAttendance("discussion-1") } returns null

        assertNull(dataSource.getMyStatus("discussion-1"))
    }

    @Test
    fun `setMyStatus inserts attendance entity`() = runTest {
        setup()
        everySuspend {
            fixture.discussionAttendanceDao.insertAttendance(DiscussionAttendanceEntity("discussion-1", "MAYBE", 0))
        } returns Unit

        dataSource.setMyStatus("discussion-1", AttendanceStatus.MAYBE)
    }

    @Test
    fun `clearMyStatus deletes existing entry`() = runTest {
        setup()
        val entity = DiscussionAttendanceEntity("discussion-1", "YES", 0)
        everySuspend { fixture.discussionAttendanceDao.getAttendance("discussion-1") } returns entity
        everySuspend { fixture.discussionAttendanceDao.deleteAttendance(entity) } returns Unit

        dataSource.clearMyStatus("discussion-1")
    }

    @Test
    fun `deleteAll clears all cached RSVPs`() = runTest {
        setup()

        dataSource.deleteAll()
    }
}
