package com.ivangarzab.kluvs.presentation.viewmodel

import com.ivangarzab.kluvs.presentation.effect.NoticeKey
import com.ivangarzab.kluvs.presentation.effect.PlatformEffect
import com.ivangarzab.kluvs.presentation.effect.PlatformEffectHandler
import com.ivangarzab.kluvs.presentation.effect.ScreenEffect
import com.ivangarzab.kluvs.presentation.effect.SnackbarMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    private data class TestState(val count: Int = 0)

    private class TestViewModel : BaseViewModel<TestState>(TestState()) {
        fun increment() = updateState { it.copy(count = currentState.count + 1) }
        fun openUrl(url: String) = send(PlatformEffect.OpenUrl(url))
        fun notify(effect: ScreenEffect) = send(effect)
    }

    private class RecordingHandler : PlatformEffectHandler {
        val handled = mutableListOf<PlatformEffect>()
        override fun handle(effect: PlatformEffect) {
            handled += effect
        }
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    @Test
    fun `updateState changes state`() {
        val viewModel = TestViewModel()

        viewModel.increment()
        viewModel.increment()

        assertEquals(TestState(count = 2), viewModel.state.value)
    }

    @Test
    fun `platform effects go to the handler from Koin`() {
        val handler = RecordingHandler()
        startKoin { modules(module { single<PlatformEffectHandler> { handler } }) }
        val viewModel = TestViewModel()

        viewModel.openUrl("https://example.com")

        assertEquals(listOf<PlatformEffect>(PlatformEffect.OpenUrl("https://example.com")), handler.handled)
    }

    @Test
    fun `a ViewModel that never sends a platform effect doesn't need Koin`() {
        // No startKoin: building and using the ViewModel must not look up the handler
        val viewModel = TestViewModel()

        viewModel.increment()

        assertEquals(1, viewModel.state.value.count)
    }

    @Test
    fun `screen effects reach the collector including ones sent before it started`() = runTest {
        val viewModel = TestViewModel()
        val early = ScreenEffect.ShowSnackbar(SnackbarMessage.Text("early"))
        val late = ScreenEffect.ShowSnackbar(SnackbarMessage.Notice(NoticeKey.SAVE_SUCCESS))
        viewModel.notify(early)

        val collected = mutableListOf<ScreenEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.screenEffects.toList(collected) }
        viewModel.notify(late)

        assertEquals(listOf<ScreenEffect>(early, late), collected)
    }
}
