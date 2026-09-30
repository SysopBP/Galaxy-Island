package com.ekoehler.expressivecutout.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekoehler.expressivecutout.data.RootMode
import com.ekoehler.expressivecutout.ui.AppViewModel

@Composable
fun RootBridgeScreen(viewModel: AppViewModel, contentPadding: PaddingValues) {
    val behaviour by viewModel.behaviour.collectAsStateWithLifecycle()
    val bridge by viewModel.privilegedBridgeState.collectAsStateWithLifecycle()
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Root & System Bridge", style = MaterialTheme.typography.headlineMedium)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Root mode", style = MaterialTheme.typography.titleMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    RootMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = behaviour.rootMode == mode,
                            onClick = { viewModel.setRootMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, RootMode.entries.size),
                        ) {
                            Text(
                                when (mode) {
                                    RootMode.OFF -> "Off"
                                    RootMode.AUTOMATIC -> "Automatic"
                                    RootMode.ENHANCED -> "Enhanced"
                                }
                            )
                        }
                    }
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Privileged bridge", style = MaterialTheme.typography.titleLarge)
                Text(if (bridge.rootAvailable) "Root ✓ available" else "Root • unavailable")
                Text(if (bridge.rootBridgeActive) "Root bridge ✓ active" else "Root bridge • fallback")
                Text(if (bridge.systemUiBridgeAvailable) "SystemUI bridge ✓ active" else "SystemUI bridge • not injected")
                Text("Watchdog: " + if (bridge.watchdogEnabled) "✓ monitoring" else "Off")
                Text("Bridge state: " + bridge.summary)
                Text("Heartbeat failures: " + bridge.consecutiveFailures)
                Text("Recovery attempts: " + bridge.recoveryAttempts)
                if (bridge.lastHeartbeatElapsedMs > 0L) {
                    val ageSeconds = ((android.os.SystemClock.elapsedRealtime() - bridge.lastHeartbeatElapsedMs) / 1000L)
                        .coerceAtLeast(0L)
                    Text("Last heartbeat: ${ageSeconds}s ago")
                }
                Button(onClick = viewModel::refreshPrivilegedBridge) { Text("Refresh bridge") }
            }
        }
        Text(
            "Automatic uses root when available and falls back to Android/Shizuku when it is not. Enhanced requests the privileged path without blocking Galaxy Island startup.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
