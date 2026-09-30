package com.ekoehler.expressivecutout.overlay

import com.ekoehler.expressivecutout.data.BehaviourSettings

/** Selects explicit alert sources; resting, media, assistant and system tiles stay quiet. */
internal fun IslandEvent.shouldLightCameraRing(settings: BehaviourSettings): Boolean = when {
    !settings.cameraRingEnabled -> false
    call != null -> settings.cameraRingCalls
    timer != null -> settings.cameraRingTimers
    media != null || assistant != null -> false
    // The ring is a visual alert, so silent notifications should still be allowed to light it.
    // Audio/haptic silence remains respected elsewhere; this only broadens visual coverage.
    progressData != null -> settings.cameraRingProgress && !progressData.isComplete
    // Live notification tiles are still real notification alerts. Treating them as a quiet
    // system tile meant many Samsung/Android 17 notifications never reached the ring even though
    // the notification ring toggle was enabled.
    liveNotificationTile != null -> settings.cameraRingNotifications
    liveDeviceTile != null -> false
    notificationKey != null -> settings.cameraRingNotifications
    else -> false
}
