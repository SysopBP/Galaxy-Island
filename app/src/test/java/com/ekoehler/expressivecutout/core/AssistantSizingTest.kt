package com.ekoehler.expressivecutout.core

import org.junit.Assert.assertEquals
import org.junit.Test

/** Covers long answers, legacy oversized preferences, and tiny viewports. */
class AssistantSizingTest {
    @Test fun longAnswerUsesTwentyPercent() { assertEquals(160, AssistantSizing.height(800, 20, 5000)) }
    @Test fun legacyEightyPercentIsCapped() { assertEquals(240, AssistantSizing.height(800, 80, 5000)) }
    @Test fun tinyScreenDoesNotThrow() { assertEquals(30, AssistantSizing.height(300, 10, 5000)) }
    @Test fun shortAnswerFits() { assertEquals(120, AssistantSizing.height(1000, 20, 120)) }
    @Test fun noDisplayHasNoHeight() { assertEquals(0, AssistantSizing.height(0, 20, 5000)) }
}
