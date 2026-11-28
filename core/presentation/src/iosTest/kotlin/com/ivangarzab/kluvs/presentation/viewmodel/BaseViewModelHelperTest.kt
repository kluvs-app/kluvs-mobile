package com.ivangarzab.kluvs.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.ivangarzab.kluvs.presentation.effect.ScreenEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelHelperTest {

    private data class TestState(val count: Int = 0)

    private class TestViewModel : BaseViewModel<TestState>(TestState()) {
        fun increment() = updateState { it.copy(count = it.count + 1) }
        fun notify(effect: ScreenEffect) = send(effect)
        fun startWork(): Job = viewModelScope.launch { awaitCancellation() }
        var cleared = false
        override fun onCleared() {
            cleared = true
        }
    }

    private class TestHelper : BaseViewModelHelper<TestState>() {
        public override val viewModel: TestViewModel by viewModel()
    }

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var scope: CoroutineScope
    private lateinit var viewModel: TestViewModel
    private lateinit var helper: TestHelper

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
        scope = CoroutineScope(dispatcher)
        startKoin {
            modules(module {
                single<CoroutineScope> { scope }
                factory { TestViewModel() }
            })
        }
        helper = TestHelper()
        viewModel = helper.viewModel
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        scope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `observeState delivers the current state and every change`() {
        val received = mutableListOf<TestState>()
        helper.observeState { received += it }

        viewModel.increment()

        assertEquals(listOf(TestState(0), TestState(1)), received)
    }

    @Test
    fun `observeScreenEffects delivers effects until closed`() {
        val received = mutableListOf<ScreenEffect>()
        val subscription = helper.observeScreenEffects { received += it }
        val first = ScreenEffect.ShowSnackbar.success("first")

        viewModel.notify(first)
        subscription.close()
        viewModel.notify(ScreenEffect.ShowSnackbar.success("after close"))

        assertEquals(listOf<ScreenEffect>(first), received)
    }

    @Test
    fun `close cancels viewModelScope and clears the ViewModel`() {
        val work = viewModel.startWork()

        helper.close()

        assertTrue(work.isCancelled)
        assertTrue(viewModel.cleared)
    }

    @Test
    fun `the ViewModel stays alive until close`() {
        val work = viewModel.startWork()

        assertTrue(work.isActive)
        assertFalse(viewModel.cleared)
    }
}
