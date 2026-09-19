package com.ekoehler.expressivecutout.service

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log

/** Plays finite notification patterns, respecting screen state, silent mode and DND. */
internal object NotificationHaptics {
    /** Plays one chosen pattern. Zero leaves vibration to Android's original notification. */
    fun play(context: Context, pattern: Int) {
        if (pattern == 0) return
        if (context.getSystemService(PowerManager::class.java)?.isInteractive != true) return
        if (context.getSystemService(AudioManager::class.java)?.ringerMode == AudioManager.RINGER_MODE_SILENT) return
        if (context.getSystemService(NotificationManager::class.java)?.currentInterruptionFilter !=
            NotificationManager.INTERRUPTION_FILTER_ALL) return
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Vibrator::class.java) ?: return
        if (!vibrator.hasVibrator()) return
        val timings = when (pattern) {
            1 -> longArrayOf(0, 35)
            2 -> longArrayOf(0, 60, 100, 60)
            else -> longArrayOf(0, 45, 80, 110, 220, 45, 80, 110)
        }
        runCatching {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, -1),
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
        }.onFailure { Log.w("NotificationHaptics", "Unable to play notification haptic", it) }
    }
}
