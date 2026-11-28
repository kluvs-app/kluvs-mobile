package com.ivangarzab.kluvs.presentation.viewmodel

import com.ivangarzab.kluvs.presentation.Closeable
import com.ivangarzab.kluvs.presentation.effect.ScreenEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Base for every `{Name}ViewModelHelper`: the iOS bridge to a [BaseViewModel]. Subclasses bind
 * [S] to their screen's state (Swift sees the concrete type) and add only action passthroughs.
 */
abstract class BaseViewModelHelper<S : Any> : KoinComponent, ScreenEffectSource {

    protected abstract val viewModel: BaseViewModel<S>
    private val coroutineScope: CoroutineScope by inject()

    fun observeState(callback: (S) -> Unit): Closeable {
        val job = viewModel.state.onEach { callback(it) }.launchIn(coroutineScope)
        return Closeable { job.cancel() }
    }

    override fun observeScreenEffects(callback: (ScreenEffect) -> Unit): Closeable {
        val job = viewModel.screenEffects.onEach { callback(it) }.launchIn(coroutineScope)
        return Closeable { job.cancel() }
    }
}
