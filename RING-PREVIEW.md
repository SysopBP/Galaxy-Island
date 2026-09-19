# Galaxy Island

A modified Expressive Cutout 0.2.0-beta for testing a Samsung-style island, persistent notifications, and a camera notification light. This is a fork of EvanKoe/expressive-cutout; the original GPL-3.0 license and credits are retained. The application ID is `app.cutout.ringpreview`, so it can install alongside upstream.

## Setup

1. Enable notification access and the accessibility overlay through the existing permission screen.
2. Under **Behaviour**, tap **Apply Samsung-style preset**. This applies rounded panels, calmer motion, a blue pulsing ring, and persistent notifications. It preserves your saved camera offsets.
3. Under **Size & position**, align the collapsed island with your camera. The ring uses that same center. Its diameter cannot exceed the collapsed island height.
4. Choose steady, pulsing, or rotating light; app accent or one of four colors; diameter, thickness, and brightness. The Behaviour screen includes a ring preview.
5. Pick System, Soft, Double, or Heartbeat haptics. **Try haptic** previews a custom pattern. Expand an app in **Apps** to override its pattern or inherit the global setting.
6. Optional reminders use the chosen custom pattern, at intervals of at least 30 seconds. Set the interval to zero to turn them off.

## Notification behavior

- With persistence enabled and timeout zero, a notification stays available until acted on through the island, dismissed, or removed by the posting app.
- New notifications and system events can temporarily replace the visible pill. Up to 32 pending notifications are retained in memory, with the latest available when the current one is dismissed. Nothing is persisted across service/process restarts.
- Set a finite timeout to allow automatic expiry. The normal-duration setting still applies when persistence is off.
- Persistent notifications remain in Android's notification panel, even if the upstream hold-notifications option is on. Dismissing one in the panel removes its bubble too.
- Turning off an app removes its pending alerts. Losing notification-listener access clears notification state.
- The ring animates only with the screen on in portrait. Screen-off/AOD lighting is not implemented. Landscape retains the upstream island behavior.
- Haptics respect silent mode and DND. Reminders require a still-active, non-silent notification. Custom patterns add a vibration to Android's existing alert; disable vibration for that app's notification channel if you want only the custom pattern.

## Build

JDK 21 for the Gradle daemon; Kotlin/Java bytecode targets Java 17. Android SDK platform 35 and Build Tools 35.0.0 are required. No new application dependencies were added. From this project directory:

```text
gradlew.bat assembleDebug testDebugUnitTest
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. Keep your signing key for subsequent installs of the same package.

## Device validation still required

This preview is not yet verified on Samsung Galaxy S26 Ultra / One UI 9 hardware. Check camera alignment at your display scaling, ring clipping during expansion, notification bursts and shade dismissal, media interruptions, screen off/on, landscape, service restart, DND, and the feel of each haptic pattern. Do not enable two island overlays simultaneously during this test.
