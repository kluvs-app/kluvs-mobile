package com.ivangarzab.kluvs.auth.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValidateCredentialsUseCaseTest {

    private val validate = ValidateCredentialsUseCase()

    @Test
    fun `valid sign up input has no errors`() {
        val errors = validate("reader@kluvs.com", "secret1", "secret1")
        assertEquals(CredentialErrors(), errors)
        assertFalse(errors.hasErrors)
    }

    @Test
    fun `blank email is required`() {
        assertEquals("Email is required", validate(" ").email)
    }

    @Test
    fun `malformed email is rejected`() {
        assertEquals("Please enter a valid email", validate("reader@kluvs").email)
        assertEquals("Please enter a valid email", validate("reader kluvs.com").email)
    }

    @Test
    fun `email-only validation skips password checks`() {
        val errors = validate("reader@kluvs.com")
        assertNull(errors.password)
        assertNull(errors.confirmPassword)
        assertFalse(errors.hasErrors)
    }

    @Test
    fun `blank password is required`() {
        assertEquals("Password is required", validate("reader@kluvs.com", "").password)
    }

    @Test
    fun `short password is rejected`() {
        assertEquals("Password must be at least 6 characters", validate("reader@kluvs.com", "12345").password)
        assertNull(validate("reader@kluvs.com", "123456").password)
    }

    @Test
    fun `mismatched confirmation is rejected`() {
        val errors = validate("reader@kluvs.com", "secret1", "secret2")
        assertEquals("Passwords must match", errors.confirmPassword)
        assertTrue(errors.hasErrors)
    }

    @Test
    fun `reports every failing field at once`() {
        val errors = validate("", "", "x")
        assertEquals("Email is required", errors.email)
        assertEquals("Password is required", errors.password)
        assertEquals("Passwords must match", errors.confirmPassword)
    }
}
