package com.ivangarzab.kluvs.data.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppVersionTest {

    @Test
    fun `parses MAJOR MINOR PATCH`() {
        assertEquals(AppVersion(1, 2, 3), AppVersion.parse("1.2.3"))
    }

    @Test
    fun `pads missing trailing parts with zero`() {
        assertEquals(AppVersion(1, 0, 0), AppVersion.parse("1"))
        assertEquals(AppVersion(1, 2, 0), AppVersion.parse("1.2"))
    }

    @Test
    fun `returns null for unparseable versions`() {
        listOf("", " ", "1.2.3.4", "1..3", "1.2.x", "v1.2.3", "1.2.3-beta", "-1.2.3").forEach {
            assertNull(AppVersion.parse(it), "Expected null for \"$it\"")
        }
    }

    @Test
    fun `compares numerically not lexically`() {
        assertTrue(AppVersion.parse("1.9.0")!! < AppVersion.parse("1.10.0")!!)
        assertTrue(AppVersion.parse("0.9.9")!! < AppVersion.parse("1.0.0")!!)
        assertTrue(AppVersion.parse("1.2.3")!! < AppVersion.parse("1.2.4")!!)
        assertEquals(0, AppVersion.parse("1.2")!!.compareTo(AppVersion.parse("1.2.0")!!))
    }
}
