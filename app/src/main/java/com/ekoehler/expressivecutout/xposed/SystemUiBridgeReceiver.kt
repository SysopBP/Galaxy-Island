package com.ekoehler.expressivecutout.xposed

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * App-process endpoint for the LSPosed SystemUI bridge.
 *
 * Stage 3 intentionally transports only event metadata. Existing notification/media/call/system
 * monitors remain authoritative for rich payloads and actions, so enabling Xposed cannot regress
 * the normal non-root path.
 */
class SystemUiBridgeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION) return
        val event = intent.getStringExtra("event") ?: return
        val owner = intent.getStringExtra("owner").orEmpty()
        val method = intent.getStringExtra("method").orEmpty()
        val args = intent.getStringExtra("args").orEmpty()
        Log.i(TAG, "GALAXY_ISLAND_NATIVE_BRIDGE event=$event target=$owner#$method args=[$args]")
        SystemUiBridgeState.record(event, owner, method)
    }

    companion object {
        const val ACTION = "app.cutout.ringpreview.action.XPOSED_SYSTEMUI_EVENT"
        private const val TAG = "GalaxyIslandBridge"
    }
}

object SystemUiBridgeState {
    @Volatile var lastEvent: String = ""
        private set
    @Volatile var lastTarget: String = ""
        private set
    @Volatile var eventCount: Long = 0
        private set
    @Volatile var lastElapsedRealtime: Long = 0
        private set

    fun record(event: String, owner: String, method: String) {
        lastEvent = event
        lastTarget = "$owner#$method"
        eventCount++
        lastElapsedRealtime = android.os.SystemClock.elapsedRealtime()
    }
}
