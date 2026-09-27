package com.ekoehler.expressivecutout.xposed
import android.app.AndroidAppHelper
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
class GalaxyIslandXposedBridge : IXposedHookLoadPackage {
 companion object { private const val SYSTEM_UI="com.android.systemui"; private const val ACTION="app.cutout.ringpreview.action.XPOSED_SYSTEMUI_EVENT"; private const val TARGET="com.ekoehler.expressivecutout.xposed.SystemUiEventReceiver"; private val heartbeatStarted=AtomicBoolean(false) }
 override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
  if (lpparam.packageName != SYSTEM_UI) return
  XposedBridge.log("GALAXY_ISLAND_XPOSED_LOADED package="+lpparam.packageName)
  val clazz=XposedHelpers.findClassIfExists("com.android.systemui.SystemUIApplication",lpparam.classLoader) ?: return
  XposedBridge.hookAllMethods(clazz,"onCreate",object:XC_MethodHook(){ override fun afterHookedMethod(param:MethodHookParam){ signal("SYSTEMUI_READY","SystemUIApplication#onCreate"); startHeartbeat() }})
 }
 private fun startHeartbeat(){ if(!heartbeatStarted.compareAndSet(false,true))return; XposedBridge.log("GALAXY_ISLAND_XPOSED_HEARTBEAT_STARTED interval=30s"); Executors.newSingleThreadScheduledExecutor{r->Thread(r,"GalaxyIsland-Xposed-Heartbeat").apply{isDaemon=true}}.scheduleAtFixedRate({runCatching{signal("HEARTBEAT","SystemUI")}},0,30,TimeUnit.SECONDS)}
 private fun signal(state:String,source:String){ val context:Context=AndroidAppHelper.currentApplication()?:return; context.sendBroadcast(Intent(ACTION).apply{setClassName("app.cutout.ringpreview",TARGET);putExtra("state",state);putExtra("source",source);putExtra("elapsedRealtime",SystemClock.elapsedRealtime())}); XposedBridge.log("GALAXY_ISLAND_XPOSED_EVENT state="+state+" source="+source)}
}