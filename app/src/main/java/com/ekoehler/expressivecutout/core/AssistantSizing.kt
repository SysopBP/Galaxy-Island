package com.ekoehler.expressivecutout.core

/** Bounds an expanded reply even when its content is long or the screen is unusually small. */
object AssistantSizing {
    /** The user-selected fraction never exceeds thirty percent of the display. */
    fun height(screenHeightDp: Int, requestedPercent: Int, contentHeightDp: Int): Int {
        val cap = (screenHeightDp.coerceAtLeast(0) * requestedPercent.coerceIn(10, 30) / 100)
        return contentHeightDp.coerceAtLeast(minOf(110, cap)).coerceAtMost(cap)
    }
}
