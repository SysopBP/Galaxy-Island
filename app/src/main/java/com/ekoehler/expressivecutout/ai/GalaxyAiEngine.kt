package com.ekoehler.expressivecutout.ai

/**
 * Small policy boundary between notification/UI code and any future local or cloud model.
 *
 * The engine is intentionally inert until both the feature and a provider are explicitly enabled.
 * No API keys, notification history, or model SDK are embedded in the run-77 preview foundation.
 */
class GalaxyAiEngine(
    private val provider: GalaxyAiProvider?,
    private val enabled: () -> Boolean,
) {
    suspend fun summarize(packageName: String, title: String?, text: String?): Result<GalaxyAiResponse> =
        run(GalaxyAiTask.SUMMARIZE_NOTIFICATION, packageName, title, text)

    suspend fun suggestReply(packageName: String, title: String?, text: String?): Result<GalaxyAiResponse> =
        run(GalaxyAiTask.SUGGEST_REPLY, packageName, title, text)

    private suspend fun run(
        task: GalaxyAiTask,
        packageName: String,
        title: String?,
        text: String?,
    ): Result<GalaxyAiResponse> {
        if (!enabled()) return Result.failure(IllegalStateException("Galaxy AI is disabled"))
        val activeProvider = provider
            ?: return Result.failure(IllegalStateException("No Galaxy AI provider configured"))
        val cleanTitle = title?.take(MAX_FIELD_CHARS)
        val cleanText = text?.take(MAX_FIELD_CHARS)
        if (cleanTitle.isNullOrBlank() && cleanText.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Notification has no text to process"))
        }
        return activeProvider.complete(
            GalaxyAiRequest(task, packageName.take(MAX_PACKAGE_CHARS), cleanTitle, cleanText)
        ).map { response ->
            response.copy(
                text = response.text.take(MAX_RESPONSE_CHARS),
                suggestedAction = when (response.suggestedAction) {
                    GalaxyAiAction.REPLY -> if (task == GalaxyAiTask.SUGGEST_REPLY) GalaxyAiAction.REPLY else GalaxyAiAction.NONE
                    GalaxyAiAction.NONE -> GalaxyAiAction.NONE
                },
            )
        }
    }

    private companion object {
        const val MAX_PACKAGE_CHARS = 256
        const val MAX_FIELD_CHARS = 4_000
        const val MAX_RESPONSE_CHARS = 2_000
    }
}
