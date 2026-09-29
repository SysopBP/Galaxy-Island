package com.ekoehler.expressivecutout.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GalaxyAiUiState(
    val notificationKey: String? = null,
    val loading: Boolean = false,
    val task: GalaxyAiTask? = null,
    val result: String? = null,
    val error: String? = null,
)

/** Process-local result channel for the experimental island AI surface. */
object GalaxyAiBus {
    private val _state = MutableStateFlow(GalaxyAiUiState())
    val state: StateFlow<GalaxyAiUiState> = _state.asStateFlow()

    fun loading(key: String?, task: GalaxyAiTask) {
        _state.value = GalaxyAiUiState(notificationKey = key, loading = true, task = task)
    }

    fun success(key: String?, task: GalaxyAiTask, text: String) {
        _state.value = GalaxyAiUiState(notificationKey = key, task = task, result = text)
    }

    fun failure(key: String?, task: GalaxyAiTask, message: String) {
        _state.value = GalaxyAiUiState(notificationKey = key, task = task, error = message)
    }

    fun clear(key: String?) {
        if (_state.value.notificationKey == key) _state.value = GalaxyAiUiState()
    }
}
