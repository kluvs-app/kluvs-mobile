package com.ivangarzab.kluvs.presentation.viewmodel

import com.ivangarzab.kluvs.presentation.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** iOS bridge base: observation that every `{Name}ViewModelHelper` repeats today. */
abstract class BaseViewModelHelper<S : Any> : KoinComponent {

    protected abstract val viewModel: BaseViewModel<S>
    private val coroutineScope: CoroutineScope by inject()

    fun observeState(callback: (S) -> Unit): Closeable {
        val job = viewModel.state.onEach { callback(it) }.launchIn(coroutineScope)
        return Closeable { job.cancel() }
    }
}
