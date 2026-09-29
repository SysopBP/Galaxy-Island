package com.ekoehler.expressivecutout.overlay

import com.ekoehler.expressivecutout.data.BehaviourSettings

/** Selects explicit alert sources; resting, media, assistant and system tiles stay quiet. */
internal fun IslandEvent.shouldLightCameraRing(settings: BehaviourSettings): Boolean = when {
    !settings.cameraRingEnabled -> false
    call != null -> settings.cameraRingCalls
    timer != null -> settings.cameraRingTimers
    media != null || assistant != null -> false
    isSilent -> false
    progressData != null -> settings.cameraRingProgress && !progressData.isComplete
    liveNotificationTile != null || liveDeviceTile != null -> false
    notificationKey != null -> settings.cameraRingNotifications
    else -> false
}
