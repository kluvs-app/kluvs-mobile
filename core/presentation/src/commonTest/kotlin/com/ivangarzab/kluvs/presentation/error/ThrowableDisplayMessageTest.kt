package com.ivangarzab.kluvs.presentation.error

import com.ivangarzab.kluvs.model.AppError
import com.ivangarzab.kluvs.model.UserFacingException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ThrowableDisplayMessageTest {

    @Test
    fun `user facing exception shows its own message`() {
        val error = UserFacingException("Cannot remove yourself from the club")

        assertEquals("Cannot remove yourself from the club", error.toDisplayMessage())
    }

    @Test
    fun `subclass of user facing exception shows its own message`() {
        class Custom : UserFacingException("Pick a different handle")

        assertEquals("Pick a different handle", Custom().toDisplayMessage())
    }

    @Test
    fun `app error maps to its friendly copy`() {
        assertEquals(AppError.NoConnection.toUserMessage(), AppError.NoConnection.toDisplayMessage())
        assertEquals(AppError.Timeout.toUserMessage(), AppError.Timeout.toDisplayMessage())
        assertEquals(AppError.ServerError.toUserMessage(), AppError.ServerError.toDisplayMessage())
    }

    @Test
    fun `unexpected exception never leaks its raw message`() {
        val raw = "java.sql.SQLException: relation \"clubs\" does not exist"

        val message = RuntimeException(raw).toDisplayMessage()

        assertEquals(AppError.Unknown.toUserMessage(), message)
        assertFalse(message.contains("SQL"))
    }

    @Test
    fun `plain illegal argument exception is not treated as user facing`() {
        val message = IllegalArgumentException("internal detail").toDisplayMessage()

        assertEquals(AppError.Unknown.toUserMessage(), message)
    }
}
