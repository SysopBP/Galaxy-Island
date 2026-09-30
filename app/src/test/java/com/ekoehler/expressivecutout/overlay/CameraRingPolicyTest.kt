package com.ekoehler.expressivecutout.overlay

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.ui.graphics.Color
import com.ekoehler.expressivecutout.core.DynamicTile
import com.ekoehler.expressivecutout.data.BehaviourSettings
import com.ekoehler.expressivecutout.service.ProgressData
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Regression coverage for alert rings accidentally lighting for every active event. */
class CameraRingPolicyTest {
    /** A real notification must remain eligible when the master switch is enabled. */
    @Test fun notificationLightsUntilItsSourceOrPreferenceIsRemoved() {
        val settings = BehaviourSettings(cameraRingEnabled = true)
        assertTrue(event().shouldLightCameraRing(settings))
        assertFalse(event().shouldLightCameraRing(settings.copy(cameraRingEnabled = false)))
        assertFalse(event().shouldLightCameraRing(settings.copy(cameraRingNotifications = false)))
        // Silent only suppresses sound/haptics; it remains eligible for the visual camera ring.
        assertTrue(event().copy(isSilent = true).shouldLightCameraRing(settings))
        assertFalse(event().copy(notificationKey = null).shouldLightCameraRing(settings))
    }

    /** Progress is opt-in, and completing a transfer stops its ring even when enabled. */
    @Test fun progressRequiresExplicitOptInAndStopsAtCompletion() {
        val settings = BehaviourSettings(cameraRingEnabled = true)
        val progress = event().copy(progressData = ProgressData(max = 100, current = 30))
        assertFalse(progress.shouldLightCameraRing(settings))
        assertTrue(progress.shouldLightCameraRing(settings.copy(cameraRingProgress = true)))
        assertFalse(progress.copy(progressData = ProgressData(max = 100, current = 100))
            .shouldLightCameraRing(settings.copy(cameraRingProgress = true)))
    }

    /** Live notification tiles use the notification ring preference just like normal alerts. */
    @Test fun liveNotificationTileUsesNotificationPreference() {
        val live = event().copy(liveNotificationTile = DynamicTile.NAVIGATION)
        assertTrue(live.shouldLightCameraRing(BehaviourSettings(cameraRingEnabled = true)))
        assertFalse(live.shouldLightCameraRing(
            BehaviourSettings(cameraRingEnabled = true, cameraRingNotifications = false)
        ))
    }

    /** Device status must never keep the notification ring running. */
    @Test fun deviceTilesStayQuiet() {
        for (tile in listOf(DynamicTile.BATTERY, DynamicTile.NETWORK, DynamicTile.BLUETOOTH_AUDIO)) {
            assertFalse(event().copy(liveDeviceTile = tile)
                .shouldLightCameraRing(BehaviourSettings(cameraRingEnabled = true)))
        }
    }

    /** Supplies the minimum rendering metadata needed to classify an alert. */
    private fun event() = IslandEvent(
        id = 1L,
        icon = IslandIcon.Vector(Icons.Rounded.Notifications),
        label = "Test notification",
        accent = Color.Blue,
        notificationKey = "notification-1",
    )
}
