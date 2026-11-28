package com.ivangarzab.kluvs.presentation.effect

import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EffectChannelTest {

    @Test
    fun `delivers effects in the order sent`() = runTest(UnconfinedTestDispatcher()) {
        val channel = EffectChannel<PlatformEffect>()
        val handled = mutableListOf<PlatformEffect>()
        val job = channel.handleWith(backgroundScope) { handled += it }

        channel.send(PlatformEffect.OpenUrl("https://example.com"))
        channel.send(PlatformEffect.OpenStoreListing)

        assertEquals(listOf(PlatformEffect.OpenUrl("https://example.com"), PlatformEffect.OpenStoreListing), handled)
        job.cancel()
    }

    @Test
    fun `buffers effects sent before handling starts`() = runTest(UnconfinedTestDispatcher()) {
        val channel = EffectChannel<PlatformEffect>()
        val handled = mutableListOf<PlatformEffect>()

        channel.send(PlatformEffect.CopyToClipboard("text"))
        channel.handleWith(backgroundScope) { handled += it }

        assertEquals(listOf<PlatformEffect>(PlatformEffect.CopyToClipboard("text")), handled)
    }

    @Test
    fun `delivers each effect only once`() = runTest(UnconfinedTestDispatcher()) {
        val channel = EffectChannel<PlatformEffect>()
        val handled = mutableListOf<PlatformEffect>()
        channel.send(PlatformEffect.ShareText("text"))

        channel.handleWith(backgroundScope) { handled += it }.cancel()
        channel.handleWith(backgroundScope) { handled += it }

        assertEquals(listOf<PlatformEffect>(PlatformEffect.ShareText("text")), handled)
    }
}
