package com.ivangarzab.kluvs.presentation.util

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatBookYearTest {

    @Test
    fun `negative year formats as BCE`() {
        assertEquals("380 BCE", formatBookYear(-380))
    }

    @Test
    fun `negative year keeps full magnitude`() {
        assertEquals("399 BCE", formatBookYear(-399))
        assertEquals("1 BCE", formatBookYear(-1))
    }

    @Test
    fun `positive year is shown as-is`() {
        assertEquals("1937", formatBookYear(1937))
    }

    @Test
    fun `positive year has no grouping separator`() {
        assertEquals("2025", formatBookYear(2025))
    }

    @Test
    fun `year zero is shown as-is`() {
        assertEquals("0", formatBookYear(0))
    }
}
