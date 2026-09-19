# Galaxy Island + Samsung Lock D2 — paired preview

## Install and connect

1. Install Galaxy-Island.apk over your current Galaxy Island preview.
2. Install SamsungLockD2-paired.apk. **The previous D2 0.3.1 APK has a different signing certificate, so it cannot be updated in place.** If you choose to use this paired build, exit D2 kiosk using your PIN and disable its wake service before removing the old D2 app. Removing it clears its PIN and settings; create a new PIN and reselect wallpaper/root access/widgets afterward. No uninstall is performed automatically.
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
