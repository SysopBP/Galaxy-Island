package com.ekoehler.expressivecutout.xposed

import android.util.Log
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.os.Handler
import android.os.Looper
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

/**
 * Galaxy Island's isolated LSPosed bridge.
 *
 * Scope is intentionally SystemUI only. Galaxy Island remains fully usable without
 * LSPosed; these probes are fail-open and do not modify SystemUI arguments/results.
 */
class GalaxyIslandXposedBridge : XposedModule() {
    companion object {
        private const val TAG = "GalaxyIslandXposed"
        private const val SYSTEM_UI = "com.android.systemui"
        private const val ACTION_BRIDGE_STATE = "app.cutout.ringpreview.SYSTEMUI_BRIDGE_STATE"
        private const val EXTRA_ACTIVE = "active"
        private const val EXTRA_HEARTBEAT = "heartbeat_elapsed"
    }

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        log(Log.INFO, TAG, "GALAXY_ISLAND_XPOSED_LOADED api=$apiVersion framework=$frameworkName")
    }

    override fun onPackageReady(param: PackageReadyParam) {
        if (param.packageName != SYSTEM_UI) return
        log(Log.INFO, TAG, "GALAXY_ISLAND_XPOSED_SYSTEMUI_READY")
        installProbes(param.classLoader)
        installBridgeHeartbeat(param.classLoader)
    }

    private fun installProbes(loader: ClassLoader) {
        val groups = mapOf(
            "SCREEN" to listOf(
                "com.android.systemui.keyguard.WakefulnessLifecycle" to listOf("dispatchStartedWakingUp", "dispatchFinishedWakingUp", "dispatchStartedGoingToSleep", "dispatchFinishedGoingToSleep"),
                "com.android.systemui.keyguard.ScreenLifecycle" to listOf("dispatchScreenTurningOn", "dispatchScreenTurnedOn", "dispatchScreenTurningOff", "dispatchScreenTurnedOff")
            ),
            "MEDIA" to listOf(
                "com.android.systemui.media.controls.pipeline.MediaDataManager" to listOf("onNotificationAdded", "onNotificationRemoved", "onNotificationUpdated", "setTimedOut")
            ),
            "CHARGING" to listOf(
                "com.android.systemui.statusbar.policy.BatteryControllerImpl" to listOf("fireBatteryLevelChanged", "firePowerSaveChanged", "onReceive")
            ),
            "CALL" to listOf(
                "com.android.systemui.statusbar.phone.ongoingcall.OngoingCallController" to listOf("updateChip", "removeChip"),
                "com.android.systemui.statusbar.phone.ongoingcall.OngoingCallControllerImpl" to listOf("updateChip", "removeChip")
            ),
            "KEYGUARD" to listOf(
                "com.android.systemui.statusbar.policy.KeyguardStateControllerImpl" to listOf("notifyKeyguardGoingAway", "notifyKeyguardState"),
                "com.android.systemui.keyguard.KeyguardViewMediator" to listOf("showLocked", "hideLocked")
            ),
            "SYSTEMUI" to listOf(
                "com.android.systemui.SystemUIApplication" to listOf("onCreate", "startServicesIfNeeded"),
                "com.android.systemui.SystemUIService" to listOf("onCreate")
            )
        )
        groups.forEach { (event, targets) -> installNamedProbes(loader, event, targets) }
    }


    /**
     * Publishes a lightweight heartbeat from the injected SystemUI process. The app uses this
     * signal only as bridge health; no SystemUI object is exported across the process boundary.
     */
    private fun installBridgeHeartbeat(loader: ClassLoader) {
        val appClass = runCatching {
            Class.forName("com.android.systemui.SystemUIApplication", false, loader)
        }.getOrNull() ?: return

        appClass.declaredMethods.filter { it.name == "onCreate" }.forEach { method ->
            runCatching {
                method.isAccessible = true
                hook(method)
                    .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                    .intercept { chain ->
                        val result = chain.proceed()
                        val context = chain.thisObject as? Context
                        context?.let { startHeartbeat(it) }
                        log(Log.INFO, TAG, "GALAXY_ISLAND_SYSTEMUI_BRIDGE_INJECTED")
                        result
                    }
            }.onFailure {
                log(Log.WARN, TAG, "GALAXY_ISLAND_SYSTEMUI_BRIDGE_INJECT_FAILED", it)
            }
        }
    }

    private fun publishHeartbeat(context: Context) {
        context.sendBroadcast(
            Intent(ACTION_BRIDGE_STATE)
                .setPackage("app.cutout.ringpreview")
                .putExtra(EXTRA_ACTIVE, true)
                .putExtra(EXTRA_HEARTBEAT, SystemClock.elapsedRealtime())
        )
    }

    private fun startHeartbeat(context: Context) {
        // SystemUI can start before Galaxy Island, so a one-shot onCreate broadcast is lossy.
        // Keep publishing while the injected process is alive; the app can attach at any time.
        val handler = Handler(Looper.getMainLooper())
        val appContext = context.applicationContext
        val beat = object : Runnable {
            override fun run() {
                runCatching { publishHeartbeat(appContext) }
                    .onFailure { log(Log.WARN, TAG, "GALAXY_ISLAND_SYSTEMUI_HEARTBEAT_FAILED", it) }
                handler.postDelayed(this, 20_000L)
            }
        }
        handler.post(beat)
    }

    private fun installNamedProbes(loader: ClassLoader, event: String, targets: List<Pair<String, List<String>>>) {
        var installed = 0
        targets.forEach { (className, names) ->
            val owner = runCatching { Class.forName(className, false, loader) }.getOrNull() ?: return@forEach
            owner.declaredMethods.filter { it.name in names }.distinctBy { it.toGenericString() }.forEach { method ->
                runCatching {
                    method.isAccessible = true
                    hook(method)
                        .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                        .intercept { chain ->
                            log(Log.INFO, TAG, "GALAXY_ISLAND_XPOSED_$event target=${owner.name}#${method.name}")
                            chain.proceed()
                        }
                    installed++
                }.onFailure {
                    log(Log.WARN, TAG, "GALAXY_ISLAND_XPOSED_${event}_HOOK_FAILED target=${owner.name}#${method.name}", it)
                }
            }
        }
        log(if (installed > 0) Log.INFO else Log.WARN, TAG, "GALAXY_ISLAND_XPOSED_${event}_READY hooks=$installed")
    }
}
