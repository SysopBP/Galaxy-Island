package com.ekoehler.expressivecutout.core

import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Reads only lock status and a lock capability from the paired, same-signer D2 app. */
class D2Client(private val context: Context) {
    /** Refuses untrusted providers, missing protocol fields, and failed IPC. */
    suspend fun state(): Bundle? = withContext(Dispatchers.IO) {
        try {
            val pm = context.packageManager
            val provider = pm.resolveContentProvider("app.d2lock.island", 0)
            if (provider?.packageName != "app.d2lock" ||
                pm.checkSignatures(context.packageName, "app.d2lock") != PackageManager.SIGNATURE_MATCH) return@withContext null
            context.contentResolver.call(URI, "state", null, null)?.takeIf {
                it.getInt("protocol") == 1 && it.get("locked") is Boolean && it.get("ready") is Boolean
            }
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Log.w("D2Client", "D2 connection unavailable", error)
            null
        }
    }

    /** Keeps the overlay private unless D2 supplied a complete unlocked response. */
    @Suppress("DEPRECATION")
    fun isLocked(state: Bundle?): Boolean = D2StatePolicy.isLocked(state?.get("protocol"), state?.get("locked"), state?.get("ready"))

    /** Returns a launch token only from a validated protocol response. */
    @Suppress("DEPRECATION")
    fun lockToken(state: Bundle): PendingIntent? = state.getParcelable("lock")

    /** The provider notifies changes without sharing notification content or PIN data. */
    companion object {
        val URI: Uri = Uri.parse("content://app.d2lock.island")
    }
}
