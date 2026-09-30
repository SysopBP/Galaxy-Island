package com.ekoehler.expressivecutout.core

import android.os.SystemClock
import com.ekoehler.expressivecutout.data.RootMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

enum class BridgeHealth { OFF, HEALTHY, FALLBACK, RECONNECTING, DEGRADED }

data class PrivilegedBridgeState(
    val mode: RootMode = RootMode.AUTOMATIC,
    val rootAvailable: Boolean = false,
    val rootBridgeActive: Boolean = false,
    val systemUiBridgeAvailable: Boolean = false,
    val fallbackActive: Boolean = true,
    val health: BridgeHealth = BridgeHealth.FALLBACK,
    val watchdogEnabled: Boolean = true,
    val recoveryAttempts: Int = 0,
    val lastRecoveryElapsedMs: Long = 0L,
    val lastHeartbeatElapsedMs: Long = 0L,
    val consecutiveFailures: Int = 0,
) {
    val summary: String
        get() = when (health) {
            BridgeHealth.OFF -> "Root enhancements off · Android fallback active"
            BridgeHealth.HEALTHY -> "Bridge ✓ healthy"
            BridgeHealth.RECONNECTING -> "Bridge reconnecting… · fallback remains active"
            BridgeHealth.DEGRADED -> "Bridge degraded ⚠ · fallback active"
            BridgeHealth.FALLBACK -> "Fallback • active"
        }
}

object PrivilegedBridge {
    suspend fun probe(
        mode: RootMode,
        previous: PrivilegedBridgeState? = null,
    ): PrivilegedBridgeState = withContext(Dispatchers.IO) {
        val now = SystemClock.elapsedRealtime()
        if (mode == RootMode.OFF) {
            return@withContext PrivilegedBridgeState(
                mode = mode, health = BridgeHealth.OFF, lastHeartbeatElapsedMs = now,
            )
        }

        val root = hasRoot()
        val failures = if (root) 0 else (previous?.consecutiveFailures ?: 0) + 1
        val wasActive = previous?.rootBridgeActive == true
        val recoveryAttempts = if (root) 0 else (previous?.recoveryAttempts ?: 0) +
            if (wasActive || failures >= 2) 1 else 0
        PrivilegedBridgeState(
            mode = mode,
            rootAvailable = root,
            rootBridgeActive = root,
            // Root does not imply an injected SystemUI hook.
            systemUiBridgeAvailable = previous?.systemUiBridgeAvailable == true && root,
            fallbackActive = !root,
            health = when {
                root -> BridgeHealth.HEALTHY
                failures >= 5 -> BridgeHealth.DEGRADED
                failures > 1 -> BridgeHealth.RECONNECTING
                else -> BridgeHealth.FALLBACK
            },
            watchdogEnabled = true,
            recoveryAttempts = recoveryAttempts,
            lastRecoveryElapsedMs = if (!root && (wasActive || failures >= 2)) now
                else previous?.lastRecoveryElapsedMs ?: 0L,
            lastHeartbeatElapsedMs = now,
            consecutiveFailures = failures,
        )
    }

    /** Slow heartbeat; failed probes back off so KernelSU is never hammered after boot. */
    fun heartbeatDelayMs(state: PrivilegedBridgeState): Long = when {
        state.mode == RootMode.OFF -> 60_000L
        state.rootBridgeActive -> 30_000L
        state.consecutiveFailures >= 5 -> 120_000L
        state.consecutiveFailures >= 2 -> 60_000L
        else -> 30_000L
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
