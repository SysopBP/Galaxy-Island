package com.ekoehler.expressivecutout.ai

import com.ekoehler.expressivecutout.data.AiSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Executes explicit user-requested AI operations. The preview provider is offline and deterministic;
 * a future cloud/local model can replace it without changing notification or overlay code.
 */
class GalaxyAiCoordinator(
    private val scope: CoroutineScope,
    private val settings: StateFlow<AiSettings>,
    provider: GalaxyAiProvider = PreviewAiProvider(),
) {
    private val engine = GalaxyAiEngine(provider) { settings.value.enabled }

    fun summarize(key: String?, packageName: String, title: String?, text: String?) {
        if (!settings.value.notificationSummaries) return
        run(key, GalaxyAiTask.SUMMARIZE_NOTIFICATION) {
            engine.summarize(packageName, title, text)
        }
    }

    fun suggestReply(key: String?, packageName: String, title: String?, text: String?) {
        if (!settings.value.suggestedReplies) return
        run(key, GalaxyAiTask.SUGGEST_REPLY) {
            engine.suggestReply(packageName, title, text)
        }
    }

    private fun run(
        key: String?,
        task: GalaxyAiTask,
        block: suspend () -> Result<GalaxyAiResponse>,
    ) {
        GalaxyAiBus.loading(key, task)
        scope.launch {
            block().fold(
                onSuccess = { GalaxyAiBus.success(key, task, it.text) },
                onFailure = { GalaxyAiBus.failure(key, task, it.message ?: "AI request failed") },
            )
        }
    }
}
