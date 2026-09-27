package com.ekoehler.expressivecutout.xposed
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
class SystemUiEventReceiver:BroadcastReceiver(){
 override fun onReceive(context:Context,intent:Intent){if(intent.action!=ACTION)return;val state=intent.getStringExtra("state")?:return;val source=intent.getStringExtra("source")?:"unknown";context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putLong(KEY_LAST_EVENT,SystemClock.elapsedRealtime()).putString(KEY_STATE,state).putString(KEY_SOURCE,source).apply();Log.i(TAG,"GALAXY_ISLAND_BRIDGE_EVENT state="+state+" source="+source)}
 companion object{const val ACTION="app.cutout.ringpreview.action.XPOSED_SYSTEMUI_EVENT";private const val PREFS="galaxy_island_xposed_health";private const val KEY_LAST_EVENT="last_event_elapsed";private const val KEY_STATE="last_state";private const val KEY_SOURCE="last_source";private const val TAG="GalaxyIslandXposed";data class Health(val status:String,val ageMs:Long?,val source:String?);fun health(context:Context,staleAfterMs:Long=90_000L):Health{val p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);val last=p.getLong(KEY_LAST_EVENT,0L);val source=p.getString(KEY_SOURCE,null);if(last<=0L)return Health("WAITING",null,source);val age=(SystemClock.elapsedRealtime()-last).coerceAtLeast(0L);return Health(if(age<=staleAfterMs)"READY" else "STALE",age,source)}}
}