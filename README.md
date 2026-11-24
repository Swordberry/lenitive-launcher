# Lenitive Launcher

A calm, minimal home launcher for Android TV and Fire TV devices. Lenitive Launcher replaces clutter with one focused app row, a shared wallpaper/screensaver slideshow, and quiet remote-driven utilities.

`Lenitive` means soothing or gently relieving — the name reflects the idea of a home screen that gets out of the way.

## Features

- Single horizontal app row with wraparound navigation and clean focus treatment.
- Custom wallpapers and a matching screensaver.
- Mixed image/video slideshow with muted, looping video playback.
- Wallpaper and screensaver share the current slide timing, including remaining time on wake.
- Hidden apps, icon changes, app reordering, and a small settings surface.
- Physical HOME press can optionally force-stop running user-installed apps via a local flag file.
- Optional external-TV quick-menu bridge using D-pad/OK/Back forwarding.
- No analytics, ads, accounts, or telemetry.

The package ID stays `com.local.homelauncher`, so this is an in-place update from the earlier My Launcher builds.

## Optional TV bridge configuration

No TV address or pairing token is included in the source or APK. To enable the external TV bridge, create this file on the device:

```text
/data/data/com.local.homelauncher/files/tv_bridge.conf
```

with these two lines:

```text
base_url=https://<tv-ip>:<api-port>
auth=<auth-token>
```

The file should be owned by the launcher UID and set to mode `0600`. If the file is missing, the bridge activity simply does nothing; the rest of the launcher is unaffected.

A separate root remote watcher is used on the Fire Stick to map physical remote buttons to the bridge and screensaver. That watcher is not part of this repository.

## Build

Use Gradle 8.7 with Android Gradle Plugin 8.4 and Android SDK 34 installed:

```sh
gradle assembleDebug
```

The unsigned source tree is provided as-is; release builds use the standard Android debug signing configuration.

## Privacy

`INTERNET` is declared only for the optional local-network TV bridge. Media files stay on-device. There is no crash reporting, advertising, or usage collection.
