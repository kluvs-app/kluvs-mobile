package com.ivangarzab.kluvs.presentation.sideeffect

import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SideEffectChannelTest {

    private class RecordingHandler : SideEffectHandler {
        val handled = mutableListOf<SideEffect>()
        override fun handle(effect: SideEffect) {
            handled += effect
        }
    }

    @Test
    fun `delivers effects to the handler in the order sent`() = runTest(UnconfinedTestDispatcher()) {
        val channel = SideEffectChannel()
        val handler = RecordingHandler()
        val job = channel.handleWith(handler, backgroundScope)

        channel.send(SideEffect.OpenUrl("https://example.com"))
        channel.send(SideEffect.OpenStoreListing)

        assertEquals(listOf(SideEffect.OpenUrl("https://example.com"), SideEffect.OpenStoreListing), handler.handled)
        job.cancel()
    }

    @Test
    fun `buffers effects sent before handling starts`() = runTest(UnconfinedTestDispatcher()) {
        val channel = SideEffectChannel()
        val handler = RecordingHandler()

        channel.send(SideEffect.CopyToClipboard("text"))
        channel.handleWith(handler, backgroundScope)

        assertEquals(listOf<SideEffect>(SideEffect.CopyToClipboard("text")), handler.handled)
    }

    @Test
    fun `delivers each effect only once`() = runTest(UnconfinedTestDispatcher()) {
        val channel = SideEffectChannel()
        val handler = RecordingHandler()
        channel.send(SideEffect.ShareText("text"))

        channel.handleWith(handler, backgroundScope).cancel()
        channel.handleWith(handler, backgroundScope)

        assertEquals(listOf<SideEffect>(SideEffect.ShareText("text")), handler.handled)
    }
}
