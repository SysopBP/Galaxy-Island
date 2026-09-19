# Galaxy Island + Samsung Lock D2 v0.4.1 — corrected paired preview

## Install and connect

1. Install Galaxy-Island.apk over your current Galaxy Island preview.
2. Install SamsungLockD2-v0.4.1-paired.apk. It uses version code 7 and the SAME signing key as the earlier paired D2 build (version code 5), so that paired build can update in place without clearing PIN/settings. This does not guarantee compatibility with separately signed public/CI APKs. If Android reports a signature mismatch, stop rather than uninstalling automatically; removal clears the PIN/settings. Exit kiosk with the PIN and disable the wake service before any deliberate reinstall.
3. Open D2, enter/create your PIN, and enable **Connect Galaxy Island (paired build)**.
4. Open Galaxy Island → Behaviour → enable **Double-tap to lock D2**. Enable **Show when empty** for a lock target even when there are no notifications.
5. Double-tap the collapsed island. D2 opens its own PIN screen. Unlock there with your D2 PIN.

The island is hidden completely while D2 is locked, its screens are foreground, or its connection is unavailable. Normal content resumes after an authenticated unlocked response and leaving D2. If D2 is absent or disconnected, turn off the Galaxy Island D2 switch to use the island independently. Camera-ring coordination on the D2 lock screen is deferred; the existing notification ring still works while unlocked.

## Compact assistant

Responses now start collapsed. Tap to expand them into a scrolling tile, defaulting to 20% of the display and adjustable from 10% to 30%. Old oversized height preferences reset to the compact default.

Assistant settings include an installed-app picker, an Open assistant button, optional long-press on the collapsed island, 12–24 sp response text, optional automatic expansion, and an independent Dismiss button. A dismissed assistant session stays hidden until the assistant closes/reopens. Choosing an app launches it; it does not guarantee its response UI can be captured. Response history is not saved. D2 privacy suppression covers assistant responses too.

## Connection and validation

Both paired APKs must use the same signing certificate. D2's provider uses a signature permission plus an exact Galaxy Island caller check. Galaxy Island checks the D2 provider owner and signer. The provider shares only typed lock status and an immutable lock-only PendingIntent. The non-exported lock activity rechecks D2 opt-in and PIN setup. There is no external unlock or preview command and no PIN exchange. Lost/malformed/unknown status keeps the overlay hidden; stale responses cannot undo a newer lock notification. Polling runs once per second only while the display is awake.

The original app IDs remain app.cutout.ringpreview and app.d2lock. No root grant is enabled automatically. D2 remains an app privacy screen with its existing optional kiosk limitations, not a replacement for Android device encryption or secure keyguard.

This is an experimental paired build. Builds and local unit tests are checked, but cross-app gestures, PIN/kiosk transitions, and layout have not been verified on a physical Samsung phone. Test manual D2 lock, double-tap lock, wrong PIN, correct PIN, screen off/on, app restart, and connection loss before relying on the integration.

Galaxy Island retains the Expressive Cutout GPL-3.0 license and attribution. D2 remains a separate application with its existing rights notice. Signing keys are not included.

## Corrected D2 base and wake troubleshooting

The first paired preview accidentally used the private repository's v0.3.1 base. This replacement uses the public v0.4.1-preview.1 tag and retains its media controls, Fahrenheit/Celsius choice, configurable shortcuts, floating action bar and notification privacy options. Only the three integration touchpoints (manifest, authenticated settings, lock/PIN transitions) differ in existing app code; the three bridge files are retained. Private repository publishing workflows are retained.

Double-tapping an empty home-screen area is a launcher action. Galaxy Island's D2 gesture is a double-tap on the collapsed camera bubble. To show D2 after waking, enter D2 settings and enable Show D2 when the screen wakes. After reinstalling, this setting and the optional root/kiosk settings default to off. If enabled but inactive, toggle the wake setting off and on and check for the Samsung Lock D2 is ready notification. If you previously used KernelSU root mode, recheck D2's root grant and optional root-mode setting. Android may block a non-root background activity launch.

The v0.4.1 wake service and root launcher are identical to the previous base. Rebuilding on v0.4.1 restores the newer features but does not establish that the reported phone wake issue is fixed. Test it on the phone. D2's PIN and Samsung's device-lock PIN are separate.
