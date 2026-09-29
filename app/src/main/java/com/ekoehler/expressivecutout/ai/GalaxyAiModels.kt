package com.ekoehler.expressivecutout.ai

/**
 * Provider-neutral contract for Galaxy Island AI.
 *
 * AI never receives Android/root capabilities directly. A provider may suggest one of the
 * allow-listed [GalaxyAiAction] values; the app remains responsible for validating and executing it.
 */
enum class GalaxyAiAction { NONE, REPLY }

enum class GalaxyAiTask { SUMMARIZE_NOTIFICATION, SUGGEST_REPLY }

data class GalaxyAiRequest(
    val task: GalaxyAiTask,
    val packageName: String,
    val title: String?,
    val text: String?,
)

data class GalaxyAiResponse(
    val text: String,
    val suggestedAction: GalaxyAiAction = GalaxyAiAction.NONE,
)

interface GalaxyAiProvider {
    val id: String
    suspend fun complete(request: GalaxyAiRequest): Result<GalaxyAiResponse>
}
