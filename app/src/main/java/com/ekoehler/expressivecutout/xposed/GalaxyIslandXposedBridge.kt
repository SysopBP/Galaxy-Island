package com.ekoehler.expressivecutout.xposed

import android.util.Log
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
    }

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        log(Log.INFO, TAG, "GALAXY_ISLAND_XPOSED_LOADED api=$apiVersion framework=$frameworkName")
    }

    override fun onPackageReady(param: PackageReadyParam) {
        if (param.packageName != SYSTEM_UI) return
        log(Log.INFO, TAG, "GALAXY_ISLAND_XPOSED_SYSTEMUI_READY")
        installProbes(param.classLoader)
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
