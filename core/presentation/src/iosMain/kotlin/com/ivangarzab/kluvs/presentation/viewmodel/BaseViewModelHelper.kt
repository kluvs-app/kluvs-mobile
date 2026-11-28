package com.ivangarzab.kluvs.presentation.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ivangarzab.kluvs.presentation.Closeable
import com.ivangarzab.kluvs.presentation.effect.ScreenEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

/**
 * Base for every `{Name}ViewModelHelper`: the iOS bridge to a [BaseViewModel]. Subclasses bind
 * [S] to their screen's state (Swift sees the concrete type) and add only action passthroughs.
 *
 * The Helper owns its ViewModel's lifetime, like a `NavBackStackEntry` does on Android: the
 * ViewModel lives in the Helper's [ViewModelStore], and [close] clears it. Subclasses declare
 * `override val viewModel: XViewModel by viewModel()`, never `by inject()`, which would hand back
 * a ViewModel no store owns and so nothing could ever clear.
 */
abstract class BaseViewModelHelper<S : Any> : KoinComponent, ScreenEffectSource {

    protected abstract val viewModel: BaseViewModel<S>
    private val coroutineScope: CoroutineScope by inject()

    @PublishedApi
    internal val viewModelStore = ViewModelStore()

    /** Resolves [VM] from Koin into this Helper's [ViewModelStore], so [close] clears it. */
    protected inline fun <reified VM : BaseViewModel<S>> viewModel(): Lazy<VM> = lazy {
        val factory = viewModelFactory { initializer { get<VM>() } }
        ViewModelProvider.create(viewModelStore, factory)[VM::class]
    }

    fun observeState(callback: (S) -> Unit): Closeable {
        val job = viewModel.state.onEach { callback(it) }.launchIn(coroutineScope)
        return Closeable { job.cancel() }
    }

    override fun observeScreenEffects(callback: (ScreenEffect) -> Unit): Closeable {
        val job = viewModel.screenEffects.onEach { callback(it) }.launchIn(coroutineScope)
        return Closeable { job.cancel() }
    }

    /**
     * Clears the ViewModel: cancels its `viewModelScope` and runs `onCleared()`. The Swift
     * Wrapper calls this from `deinit`, when its screen is gone for good.
     */
    fun close() {
        viewModelStore.clear()
    }
}
