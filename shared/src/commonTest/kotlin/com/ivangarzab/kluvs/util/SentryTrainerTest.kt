package com.ivangarzab.kluvs.util

import com.ivangarzab.bark.Level
import com.ivangarzab.bark.Pack
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for [SentryTrainer].
 *
 * Note: These tests verify the trainer's configuration and logic.
 * Full integration testing with Sentry requires a real Sentry instance.
 */
class SentryTrainerTest {

    @Test
    fun testTrainerConfiguration() {
        // Given: a new SentryTrainer instance
        val trainer = SentryTrainer()

        // Then: should have correct configuration
        assertEquals(Level.INFO, trainer.minLevel, "Volume should be INFO")
        assertEquals(Pack.CUSTOM, trainer.pack, "Pack should be CUSTOM")
    }

    @Test
    fun testHandleDoesNotCrashWithInfoLevel() = runTest {
        // Given: a SentryTrainer instance
        val trainer = SentryTrainer()

        // When: handling an INFO level log
        // Then: should not crash
        trainer.handle(
            level = Level.INFO,
            tag = "TestTag",
            message = "Test info message",
            throwable = null
        )
    }

    @Test
    fun testHandleDoesNotCrashWithWarningLevel() = runTest {
        // Given: a SentryTrainer instance
        val trainer = SentryTrainer()

        // When: handling a WARNING level log
        // Then: should not crash
        trainer.handle(
            level = Level.WARNING,
            tag = "TestTag",
            message = "Test warning message",
            throwable = null
        )
    }

    @Test
    fun testHandleDoesNotCrashWithErrorLevel() = runTest {
        // Given: a SentryTrainer instance
        val trainer = SentryTrainer()

        // When: handling an ERROR level log
        // Then: should not crash
        trainer.handle(
            level = Level.ERROR,
            tag = "TestTag",
            message = "Test error message",
            throwable = null
        )
    }

    @Test
    fun testHandleDoesNotCrashWithCriticalLevel() = runTest {
        // Given: a SentryTrainer instance
        val trainer = SentryTrainer()

        // When: handling a CRITICAL level log
        // Then: should not crash
        trainer.handle(
            level = Level.CRITICAL,
            tag = "TestTag",
            message = "Test critical message",
            throwable = null
        )
    }

    @Test
    fun testHandleDoesNotCrashWithThrowable() = runTest {
        // Given: a SentryTrainer instance and a throwable
        val trainer = SentryTrainer()
        val testException = RuntimeException("Test exception")

        // When: handling an ERROR with a throwable
        // Then: should not crash
        trainer.handle(
            level = Level.ERROR,
            tag = "TestTag",
            message = "Test error with exception",
            throwable = testException
        )
    }

    @Test
    fun testHandleDoesNotCrashWithNullThrowable() = runTest {
        // Given: a SentryTrainer instance
        val trainer = SentryTrainer()

        // When: handling a CRITICAL level log without throwable
        // Then: should not crash (should capture message instead)
        trainer.handle(
            level = Level.CRITICAL,
            tag = "TestTag",
            message = "Test critical message without exception",
            throwable = null
        )
    }

    @Test
    fun testTrainerCanBeInstantiated() {
        // Given/When: creating a SentryTrainer
        val trainer = SentryTrainer()

        // Then: should be successfully created
        assertNotNull(trainer)
    }
}
