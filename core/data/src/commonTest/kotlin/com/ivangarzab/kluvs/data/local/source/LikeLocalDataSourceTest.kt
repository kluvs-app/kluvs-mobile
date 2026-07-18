package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.kluvs.data.DatabaseMockFixture
import com.ivangarzab.kluvs.database.entities.LikeEntity
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LikeLocalDataSourceTest {

    private lateinit var fixture: DatabaseMockFixture
    private lateinit var dataSource: LikeLocalDataSource

    private fun setup() {
        fixture = DatabaseMockFixture()
        dataSource = LikeLocalDataSourceImpl(fixture.database)
    }

    @Test
    fun `getLikeStatus returns liked state when cached`() = runTest {
        setup()
        everySuspend { fixture.likeDao.getLike("book-1") } returns LikeEntity("book-1", true, 0)

        assertEquals(true, dataSource.getLikeStatus("book-1"))
    }

    @Test
    fun `getLikeStatus returns null when not cached`() = runTest {
        setup()
        everySuspend { fixture.likeDao.getLike("book-1") } returns null

        assertNull(dataSource.getLikeStatus("book-1"))
    }

    @Test
    fun `setLikeStatus inserts like entity`() = runTest {
        setup()
        everySuspend { fixture.likeDao.insertLike(LikeEntity("book-1", true, 0)) } returns Unit

        dataSource.setLikeStatus("book-1", true)
    }

    @Test
    fun `deleteAll clears all likes`() = runTest {
        setup()

        dataSource.deleteAll()
    }
}
