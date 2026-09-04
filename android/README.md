# Apex Innovators — Android Application

Native Android application for the **Apex Innovators** platform (`https://sky-ydv2008.github.io/Team.Apex/`).

Built in **Kotlin** with a full-screen, hardware-accelerated WebView engine, pull-to-refresh, offline detection, native back navigation, file upload support, and brand-matching dark mode.

---

## 📱 Features

- **Hardware-Accelerated WebView**: Smooth 60/120fps scrolling with full DOM storage and database persistence for login sessions.
- **Swipe-to-Refresh (`SwipeRefreshLayout`)**: Native pull-down to refresh with brand cyan spinner (`#22D3EE`).
- **Offline Mode & Error Handling**: Custom offline fallback screen with a "Retry Connection" button when network connectivity is lost.
- **File Upload Support**: Native `WebChromeClient` file chooser launcher for image and document uploads.
- **Native Download Manager**: Direct file and certificate downloads delegated to Android's native `DownloadManager`.
- **Intelligent URL Routing**: Internal platform links stay inside the app; external links (`tel:`, `mailto:`, GitHub, LinkedIn, WhatsApp) open in their native external apps.
- **Dual-Press Back to Exit**: Standard Android back button navigates web history; double-tap on root page exits cleanly.
- **Modern Adaptive App Icon**: Tailored launcher icons for Android 8.0+ (API 26+) through Android 15 (API 35).
- **Deep Linking**: Configured intent filters for `https://sky-ydv2008.github.io/Team.Apex/*`.

---

## 🛠️ Tech Stack & Requirements

- **Language:** Kotlin 2.0+
- **Minimum SDK:** Android 7.0 (API level 24) — covers >99% of Android devices worldwide
- **Target / Compile SDK:** Android 15 (API level 35)
- **Java Compatibility:** JDK 17
- **Build System:** Gradle with Kotlin DSL (`build.gradle.kts`)

---

## 🚀 How to Open and Build in Android Studio

### 1. Open in Android Studio
1. Open **Android Studio** (Koala / Ladybug or newer).
2. Click **File** $\rightarrow$ **Open...**
3. Browse to and select the `android` folder:
   ```
   C:\Users\ODIN\Desktop\Team.Apex\android
   ```
4. Click **OK**. Android Studio will sync the Gradle project automatically.

### 2. Run on an Emulator or Device
1. Connect an Android phone with USB debugging enabled, or start an Android Virtual Device (AVD).
2. Click the green **Run ▶** button (or press `Shift + F10`).

### 3. Generate Installable APK
1. In Android Studio, go to **Build** $\rightarrow$ **Build Bundle(s) / APK(s)** $\rightarrow$ **Build APK(s)**.
2. Once the build finishes, click the **locate** popup link to find your installable `app-debug.apk`.
3. Transfer the APK to any Android phone and install!

### Or build via Command Line (if JDK is installed):
```bash
cd android
./gradlew assembleDebug
```
The resulting APK will be at:
```
app/build/outputs/apk/debug/app-debug.apk
```
