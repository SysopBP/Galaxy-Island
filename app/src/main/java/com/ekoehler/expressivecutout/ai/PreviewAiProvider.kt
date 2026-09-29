package com.ekoehler.expressivecutout.ai

/**
 * Offline provider used only to exercise the Galaxy AI UI and policy path.
 * It performs no network I/O and never receives device/root capabilities.
 */
class PreviewAiProvider : GalaxyAiProvider {
    override val id: String = "preview-local"

    override suspend fun complete(request: GalaxyAiRequest): Result<GalaxyAiResponse> {
        val source = listOfNotNull(request.title, request.text)
            .joinToString(": ")
            .trim()
            .replace(Regex("\\s+"), " ")
        if (source.isBlank()) return Result.failure(IllegalArgumentException("No notification text"))
        return when (request.task) {
            GalaxyAiTask.SUMMARIZE_NOTIFICATION -> Result.success(
                GalaxyAiResponse(source.take(180))
            )
            GalaxyAiTask.SUGGEST_REPLY -> Result.success(
                GalaxyAiResponse(
                    text = "Thanks — I saw your message and will get back to you shortly.",
                    suggestedAction = GalaxyAiAction.REPLY,
                )
            )
        }
    }
}
