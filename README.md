# Taim (たいむ) ⏱️

> A minimalist, fully transparent Android home screen clock and battery widget powered by Kotlin and Jetpack Glance.

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Android Gradle Plugin](https://img.shields.io/badge/AGP-8.2.2-green.svg?logo=android)](https://developer.android.com/studio/releases/gradle-plugin)
[![Jetpack Glance](https://img.shields.io/badge/Jetpack%20Glance-1.0.0-4285F4.svg?logo=android)](https://developer.android.com/jetpack/compose/glance)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-brightgreen.svg)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34%20(Android%2014)-blue.svg)](https://developer.android.com)

---

## 🌟 Overview

**Taim** is an ultra-minimalist Android home screen widget designed with aesthetics and battery efficiency at its core. It strips away all heavy background boxes and containers, placing crisp white typography and icons directly on your home screen wallpaper.

To maintain optimal legibility even against pure white or vibrant, colorful wallpapers, subtle soft drop shadows are applied to all text and elements.

---

## 📐 Layout & Visual Design

The widget features a vertically centered single-column layout:

1. **Digital 24-Hour Time**:
   - The prominent centerpiece, rendered at **70sp** with system **SemiBold** weight (`sans-serif-medium`).
   - Soft drop shadow (`#80000000`, 4dp blur radius).
   - Driven natively by SystemUI (`TextClock`), eliminating background CPU wakeups and ensuring 100% time accuracy with 0% battery impact.

2. **Extended Date**:
   - Positioned immediately beneath the clock.
   - Formatted in extended locale style (e.g., `mercoledì 28 ottobre` / `Wednesday 28 October`).
   - Regular weight (`sans-serif`) at **15sp** with subtle drop shadow.

3. **Battery Row**:
   - Horizontally aligned Material Design vector icon and numerical percentage (e.g., `85%`).
   - Dynamically swaps to a charging icon with a lightning bolt when connected to power.
   - Text rendered at **14sp SemiBold** with simulated drop shadow.

---

## 🏗️ Architecture & Technical Highlights

```
io.github.ar3ac.taim
├── TaimApp.kt                     # Application class; registers dynamic ACTION_BATTERY_CHANGED
├── MainActivity.kt                # Companion UI with live preview and "Add to Home Screen" pin action
├── model
│   └── BatteryState.kt            # Data model holding percentage & isCharging
├── receiver
│   └── BatteryReceiver.kt        # BroadcastReceiver handling battery & power state changes
├── util
│   └── BatteryUtils.kt           # Sticky intent reader for instant synchronous battery parsing
└── widget
    ├── TaimWidget.kt             # Jetpack GlanceAppWidget implementation
    └── TaimWidgetReceiver.kt     # GlanceAppWidgetReceiver connecting system widget lifecycle
```

### 1. Accurate Time Without Battery Drain (`AndroidRemoteViews`)
Standard Compose/Glance widgets that attempt to tick time using `WorkManager` or background alarms get throttled by Android 12+ Doze mode and drain battery. **Taim** solves this by embedding an Android `TextClock` inside Glance using `AndroidRemoteViews`. The system launcher updates the clock in real-time within SystemUI with zero process wakes.

### 2. Dynamic Battery Lifecycle (`ACTION_BATTERY_CHANGED`)
- On Android 8.0+ (API 26+), `ACTION_BATTERY_CHANGED` is restricted and cannot be declared statically in `AndroidManifest.xml`.
- Taim dynamically registers `BatteryReceiver` inside `TaimApp` using `ContextCompat.registerReceiver` with `RECEIVER_EXPORTED` (Android 14+ compatible).
- In addition, power state broadcasts (`ACTION_POWER_CONNECTED` and `ACTION_POWER_DISCONNECTED`) are declared in `AndroidManifest.xml` so that plugging/unplugging immediately wakes the widget even if the app process was dormant.
- On widget instantiation, `BatteryUtils.getBatteryState()` synchronously retrieves the cached sticky intent from Android so there is never a blank or stale initial state.

---

## 🌍 Internationalization (i18n)

Taim supports internationalization out of the box:
- **English (`en`)**: Default
- **Italian (`it`)**: Full translation (`values-it/strings.xml`)

---

## 🚀 Building & Installation

### Requirements
- Android SDK 34 (compileSdk: 34, minSdk: 26)
- JDK 17 or JDK 21

### Build Debug APK
```bash
./gradlew assembleDebug
```

The APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

### Install to Device via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License
This project is open-source and available under the Apache 2.0 License.

