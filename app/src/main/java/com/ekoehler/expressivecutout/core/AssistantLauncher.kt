package com.ekoehler.expressivecutout.core

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

/** Opens a user-chosen assistant app, or delegates to Android's configured assistant. */
object AssistantLauncher {
    /** Reports missing apps visibly instead of silently changing the selected provider. */
    fun open(context: Context, packageName: String?) {
        try {
            val intent = if (packageName == null) Intent(Intent.ACTION_VOICE_COMMAND)
                else context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent == null) throw android.content.ActivityNotFoundException()
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (error: Exception) {
            Log.w("AssistantLauncher", "Assistant unavailable", error)
            Toast.makeText(context, "Assistant unavailable. Choose an installed app in Assistant settings.", Toast.LENGTH_LONG).show()
        }
    }
}
