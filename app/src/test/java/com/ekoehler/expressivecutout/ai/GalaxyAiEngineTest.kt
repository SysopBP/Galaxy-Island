package com.ekoehler.expressivecutout.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GalaxyAiEngineTest {
    @Test fun disabledEngineNeverCallsProvider() = runBlocking {
        var called = false
        val provider = object : GalaxyAiProvider {
            override val id = "test"
            override suspend fun complete(request: GalaxyAiRequest): Result<GalaxyAiResponse> {
                called = true
                return Result.success(GalaxyAiResponse("unexpected"))
            }
        }
        val result = GalaxyAiEngine(provider) { false }.summarize("pkg", "title", "text")
        assertTrue(result.isFailure)
        assertEquals(false, called)
    }

    @Test fun summaryCannotSmuggleReplyAction() = runBlocking {
        val provider = object : GalaxyAiProvider {
            override val id = "test"
            override suspend fun complete(request: GalaxyAiRequest) =
                Result.success(GalaxyAiResponse("short summary", GalaxyAiAction.REPLY))
        }
        val result = GalaxyAiEngine(provider) { true }.summarize("pkg", "title", "text").getOrThrow()
        assertEquals(GalaxyAiAction.NONE, result.suggestedAction)
    }
}
