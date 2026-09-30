package com.ekoehler.expressivecutout.system

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-lifetime camera availability observer used by the physical cutout ring.
 *
 * CameraManager reports a camera as unavailable while a client owns it. We retain a set instead
 * of a single boolean because Samsung switches between public and logical/hidden camera IDs
 * (for example front 1 and rear logical 20); a switch can make one ID available before the next
 * becomes unavailable.
 */
class CameraUsageMonitor(context: Context) {
    private val cameraManager = context.getSystemService(CameraManager::class.java)
    private val thread = HandlerThread("GalaxyIslandCameraObserver")
    private lateinit var handler: Handler
    private val unavailable = linkedSetOf<String>()
    private val knownDeviceCameras = linkedSetOf<String>()

    private val callback = object : CameraManager.AvailabilityCallback() {
        override fun onCameraUnavailable(cameraId: String) {
            synchronized(unavailable) {
                unavailable += cameraId
                publish("unavailable", cameraId)
            }
        }

        override fun onCameraAvailable(cameraId: String) {
            synchronized(unavailable) {
                unavailable -= cameraId
                publish("available", cameraId)
            }
        }
    }

    fun start() {
        if (thread.isAlive) return
        thread.start()
        handler = Handler(thread.looper)
        runCatching {
            cameraManager.cameraIdList.forEach { id ->
                val facing = cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.LENS_FACING)
                if (facing == CameraCharacteristics.LENS_FACING_FRONT ||
                    facing == CameraCharacteristics.LENS_FACING_BACK
                ) knownDeviceCameras += id
            }
        }.onFailure { Log.w(TAG, "camera-id-scan failed", it) }

        Log.i(TAG, "REGISTER knownIds=$knownDeviceCameras")
        cameraManager.registerAvailabilityCallback(callback, handler)
    }

    fun stop() {
        runCatching { cameraManager.unregisterAvailabilityCallback(callback) }
        synchronized(unavailable) {
            unavailable.clear()
            _active.value = false
        }
        if (thread.isAlive) thread.quitSafely()
        Log.i(TAG, "UNREGISTER")
    }

    private fun publish(reason: String, cameraId: String) {
        // Keep IDs learned from Samsung callbacks too: logical/hidden IDs are not guaranteed to be
        // returned by cameraIdList, but availability callbacks can still expose them.
        knownDeviceCameras += cameraId
        val activeIds = unavailable.filterTo(linkedSetOf()) { it in knownDeviceCameras }
        val nowActive = activeIds.isNotEmpty()
        _active.value = nowActive
        Log.i(TAG, "observer $reason id=$cameraId active=$nowActive activeIds=$activeIds")
    }

    companion object {
        private const val TAG = "GalaxyIslandCameraRing"
        private val _active = MutableStateFlow(false)
        val active: StateFlow<Boolean> = _active.asStateFlow()
    }
}
