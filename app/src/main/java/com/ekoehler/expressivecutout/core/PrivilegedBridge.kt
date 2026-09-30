package com.ekoehler.expressivecutout.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Optional privileged capability bridge.
 *
 * Root is never required for Galaxy Island startup. Every probe is bounded and failures resolve
 * to an unavailable capability so the normal Android/Shizuku paths remain usable after a reboot.
 */
enum class RootMode { OFF, AUTOMATIC, ENHANCED }

data class PrivilegedBridgeState(
    val mode: RootMode = RootMode.AUTOMATIC,
    val rootAvailable: Boolean = false,
    val rootBridgeActive: Boolean = false,
    val systemUiBridgeAvailable: Boolean = false,
    val fallbackActive: Boolean = true,
) {
    val summary: String
        get() = when {
            mode == RootMode.OFF -> "Root enhancements off · Android fallback active"
            rootBridgeActive -> "Root bridge active"
            else -> "Root unavailable · Android fallback active"
        }
}

object PrivilegedBridge {
    suspend fun probe(mode: RootMode): PrivilegedBridgeState = withContext(Dispatchers.IO) {
        if (mode == RootMode.OFF) return@withContext PrivilegedBridgeState(mode = mode)
        val root = hasRoot()
        PrivilegedBridgeState(
            mode = mode,
            rootAvailable = root,
            rootBridgeActive = root,
            // Injection into SystemUI is a separate capability; root alone must never claim it.
            systemUiBridgeAvailable = false,
            fallbackActive = !root,
        )
    }

    private fun hasRoot(): Boolean = runCatching {
        val process = ProcessBuilder("su", "-c", "id -u")
            .redirectErrorStream(true)
            .start()
        if (!process.waitFor(1500, TimeUnit.MILLISECONDS)) {
            process.destroyForcibly()
            false
        } else {
            process.exitValue() == 0 && process.inputStream.bufferedReader().readText().trim() == "0"
        }
    }.getOrDefault(false)
}
