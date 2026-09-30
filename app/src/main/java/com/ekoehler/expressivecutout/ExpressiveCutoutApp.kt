package com.ekoehler.expressivecutout

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.ekoehler.expressivecutout.system.PermissionUsageMonitor
import com.ekoehler.expressivecutout.core.PrivilegedBridge
import com.ekoehler.expressivecutout.system.ShizukuState
import com.ekoehler.expressivecutout.system.StatusBarIconController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.lsposed.hiddenapibypass.HiddenApiBypass

/**
 * Application entry point. Owns the process-lifetime pieces of the Shizuku bridge: the hidden-API
 * exemption, the binder listeners behind [ShizukuState], and the coroutine scope that re-applies the
 * status-bar flags and re-reads permission usage whenever Shizuku reconnects.
 */
class ExpressiveCutoutApp : Application() {

    private val systemUiBridgeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != ACTION_SYSTEMUI_BRIDGE_STATE) return
            if (!intent.getBooleanExtra("active", false)) return
            val heartbeat = intent.getLongExtra("heartbeat_elapsed", android.os.SystemClock.elapsedRealtime())
            PrivilegedBridge.noteSystemUiHeartbeat(heartbeat)
        }
    }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Starts the singletons that have to outlive any single service or activity, and lifts the
     * hidden-API restriction they need, before anything else in the process runs.
     */
    override fun onCreate() {
        super.onCreate()
        // IStatusBarService is a non-SDK interface, so plain reflection on it is blocked for apps
        // targeting a recent SDK. This lifts the restriction for our process only.
        HiddenApiBypass.addHiddenApiExemptions("")
        ShizukuState.start(this)
        StatusBarIconController.start(this, appScope)
        PermissionUsageMonitor.start(this, appScope)
        registerReceiver(
            systemUiBridgeReceiver,
            IntentFilter(ACTION_SYSTEMUI_BRIDGE_STATE),
            Context.RECEIVER_EXPORTED,
        )
    }

    companion object {
        private const val ACTION_SYSTEMUI_BRIDGE_STATE = "app.cutout.ringpreview.SYSTEMUI_BRIDGE_STATE"
    }
}
