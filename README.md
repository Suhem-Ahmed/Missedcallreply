# MissedCall Reply — Android App

Auto-sends an SMS to anyone who missed-calls you, with two modes:
- **All missed calls** — always on
- **Time window** — only between your set hours (e.g. 10:00–15:00)

---

## How to Build & Install

### Option A — Android Studio (easiest)
1. Download and install [Android Studio](https://developer.android.com/studio)
2. Open Android Studio → **Open** → select the `MissedCallReply` folder
3. Wait for Gradle sync to finish
4. Connect your phone via USB (enable USB Debugging in Developer Options)
5. Click the **Run ▶** button
6. The APK installs directly on your phone

### Option B — Command Line
```bash
cd MissedCallReply
./gradlew assembleDebug
# APK will be at: app/build/outputs/apk/debug/app-debug.apk
# Transfer to phone and install
```

---

## After Installing
1. Open the app
2. Grant all permissions when asked (Phone, SMS, Call Log)
3. Go to phone Settings → Apps → MissedCall Reply → Battery → set to **Unrestricted**
4. Choose your mode (All calls OR Time window)
5. Set your time window if needed
6. Type your auto-reply message
7. Tap **SAVE SETTINGS**

That's it. The app runs a background service and auto-restarts after phone reboot.

---

## Files
```
app/src/main/java/com/suhem/missedcallreply/
  MainActivity.java       — UI screen
  CallReceiver.java       — Detects missed calls, sends SMS
  CallMonitorService.java — Foreground service (keeps app alive)
  BootReceiver.java       — Auto-starts after phone reboot
```
