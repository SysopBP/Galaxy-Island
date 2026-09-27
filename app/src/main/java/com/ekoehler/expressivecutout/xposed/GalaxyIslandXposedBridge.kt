package com.ekoehler.expressivecutout.xposed

import android.util.Log
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

class GalaxyIslandXposedBridge : XposedModule() {
    companion object {
        private const val TAG = "GalaxyIslandXposed"
        private const val SYSTEM_UI = "com.android.systemui"
        private const val APP_PACKAGE = "app.cutout.ringpreview"
        private const val ACTION_SYSTEMUI_EVENT = "app.cutout.ringpreview.action.XPOSED_SYSTEMUI_EVENT"
    }

    /**
     * Stage 3 transport is intentionally log-backed for now. libxposed runs this module inside
     * SystemUI, but its compile API does not expose legacy AndroidAppHelper/hidden ActivityThread.
     * Keep hooks safe and observable while the app-side bridge is upgraded to a supported IPC path.
     */
    private fun emitSystemUiEvent(event: String, owner: String, method: String, args: String? = null) {
        val payload = args?.take(2048).orEmpty()
        log(Log.INFO, TAG, "GALAXY_ISLAND_NATIVE_EVENT event=$event target=$owner#$method args=[$payload]")
    }

    override fun onModuleLoaded(param: ModuleLoadedParam) { log(Log.INFO, TAG, "GALAXY_ISLAND_XPOSED_LOADED api=$apiVersion framework=$frameworkName") }
    override fun onPackageReady(param: PackageReadyParam) {
        if (param.packageName != SYSTEM_UI) return
        log(Log.INFO, TAG, "GALAXY_ISLAND_SYSTEMUI_READY package=${param.packageName}")
        installScreenLifecycleDiagnostics(param.classLoader)
        installStage2SystemUiDiagnostics(param.classLoader)
    }
    private fun installStage2SystemUiDiagnostics(classLoader: ClassLoader) {
        installNamedDiagnostics(classLoader, "NOTIFICATION", listOf(
            "com.android.systemui.statusbar.notification.collection.NotifCollection" to listOf("onNotificationPosted", "onNotificationRemoved", "onNotificationRankingUpdate"),
            "com.android.systemui.statusbar.notification.NotificationEntryManager" to listOf("addNotification", "updateNotification", "removeNotification")
        ))
        installNamedDiagnostics(classLoader, "MEDIA", listOf(
            "com.android.systemui.media.controls.pipeline.MediaDataManager" to listOf("onNotificationAdded", "onNotificationRemoved", "onNotificationUpdated", "setTimedOut")
        ))
        installNamedDiagnostics(classLoader, "CHARGING", listOf(
            "com.android.systemui.statusbar.policy.BatteryControllerImpl" to listOf("fireBatteryLevelChanged", "firePowerSaveChanged", "onReceive")
        ))
        installNamedDiagnostics(classLoader, "CALL", listOf(
            "com.android.systemui.statusbar.phone.ongoingcall.OngoingCallController" to listOf("updateChip", "removeChip"),
            "com.android.systemui.statusbar.phone.ongoingcall.OngoingCallControllerImpl" to listOf("updateChip", "removeChip")
        ))
    }

    private fun installNamedDiagnostics(
        classLoader: ClassLoader,
        event: String,
        targets: List<Pair<String, List<String>>>
    ) {
        var installed = 0
        targets.forEach { (className, names) ->
            val owner = runCatching { Class.forName(className, false, classLoader) }.getOrNull() ?: return@forEach
            owner.declaredMethods.filter { it.name in names }.distinctBy { it.toGenericString() }.forEach { method ->
                runCatching {
                    method.isAccessible = true
                    hook(method).setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE).intercept { chain ->
                        val args = chain.args.joinToString(",") { arg ->
                            when (arg) {
                                null -> "null"
                                is Boolean, is Number, is String, is Enum<*> -> arg.toString()
                                else -> arg.javaClass.name
                            }
                        }
                        log(Log.INFO, TAG, "GALAXY_ISLAND_SYSTEMUI_${event} target=${owner.name}#${method.name} args=[$args]")
                        emitSystemUiEvent(event, owner.name, method.name, args)
                        chain.proceed()
                    }
                    installed++
                    log(Log.INFO, TAG, "GALAXY_ISLAND_SYSTEMUI_${event}_HOOK_INSTALLED target=${owner.name}#${method.name}")
                }.onFailure {
                    log(Log.WARN, TAG, "GALAXY_ISLAND_SYSTEMUI_${event}_HOOK_FAILED target=${owner.name}#${method.name}", it)
                }
            }
        }
        if (installed == 0) {
            log(Log.WARN, TAG, "GALAXY_ISLAND_SYSTEMUI_${event}_UNAVAILABLE")
        } else {
            log(Log.INFO, TAG, "GALAXY_ISLAND_SYSTEMUI_${event}_READY hooks=$installed")
        }
    }

    private fun installScreenLifecycleDiagnostics(classLoader: ClassLoader) {
        val targets = listOf(
            "com.android.systemui.keyguard.WakefulnessLifecycle" to listOf("dispatchStartedWakingUp", "dispatchFinishedWakingUp", "dispatchStartedGoingToSleep", "dispatchFinishedGoingToSleep"),
            "com.android.systemui.keyguard.ScreenLifecycle" to listOf("dispatchScreenTurningOn", "dispatchScreenTurnedOn", "dispatchScreenTurningOff", "dispatchScreenTurnedOff")
        )
        var installed = 0
        targets.forEach { (className, names) ->
            val owner = runCatching { Class.forName(className, false, classLoader) }.getOrNull() ?: return@forEach
            owner.declaredMethods.filter { it.name in names }.distinctBy { it.toGenericString() }.forEach { method ->
                runCatching {
                    method.isAccessible = true
                    hook(method).setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE).intercept { chain ->
                        log(Log.INFO, TAG, "GALAXY_ISLAND_SYSTEMUI_SCREEN target=${owner.name}#${method.name}")
                        emitSystemUiEvent("SCREEN", owner.name, method.name)
                        chain.proceed()
                    }
                    installed++
                    log(Log.INFO, TAG, "GALAXY_ISLAND_SYSTEMUI_HOOK_INSTALLED target=${owner.name}#${method.name}")
                }.onFailure { log(Log.WARN, TAG, "GALAXY_ISLAND_SYSTEMUI_HOOK_FAILED target=${owner.name}#${method.name}", it) }
            }
        }
        log(Log.INFO, TAG, "GALAXY_ISLAND_SYSTEMUI_DIAGNOSTICS_READY hooks=$installed")
    }
}
