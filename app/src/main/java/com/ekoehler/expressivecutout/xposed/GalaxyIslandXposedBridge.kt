package com.ekoehler.expressivecutout.xposed

import android.util.Log
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

class GalaxyIslandXposedBridge : XposedModule() {
    companion object { private const val TAG = "GalaxyIslandXposed"; private const val SYSTEM_UI = "com.android.systemui" }
    override fun onModuleLoaded(param: ModuleLoadedParam) { log(Log.INFO, TAG, "GALAXY_ISLAND_XPOSED_LOADED api=$apiVersion framework=$frameworkName") }
    override fun onPackageReady(param: PackageReadyParam) {
        if (param.packageName != SYSTEM_UI) return
        log(Log.INFO, TAG, "GALAXY_ISLAND_SYSTEMUI_READY package=${param.packageName}")
        installScreenLifecycleDiagnostics(param.classLoader)
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
