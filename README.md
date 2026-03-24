# CLOSED PROJETC
# Qalqon (Android Offline Security App)

Qalqon is an Android security monitor that watches device storage for incoming APK files and warns users before installation.

## Implemented core behavior

- Foreground `ApkMonitorService` runs as a 24/7 monitor.
- Dual detection path:
  - `DownloadCompleteReceiver` reacts when a download finishes.
  - `FileMonitorManager` uses `FileObserver` on high-risk folders (Downloads, Telegram, shared paths) with subfolder watchers.
  - `ApkRescanWorker` performs periodic offline rescans to recover missed filesystem events.
- Source trust validation via hybrid logic:
  - Google Play/system-like paths treated as trusted.
  - Telegram, browser downloads, shared folders treated as untrusted.
  - `ApkTrustAnalyzer` compares APK signing cert with installed package cert when possible to reduce false alarms.
- Immediate risk response for untrusted APK:
  - High-priority alert notification.
  - Full-screen `AlertActivity` with actions:
    - Delete APK
    - Keep Anyway
    - More Info
- Offline local reporting with Room database:
  - detected APK events
  - source label
  - timestamps
  - user action
  - suspicious link scan events
- Suspicious link warning module (`LinkRiskAnalyzer`) in-app.
- Settings include theme switch and permission management shortcut.
- Settings include monitoring toggle and alert toggle.

## Main modules

- `app/src/main/java/com/qalqon/security/monitor/` - monitoring pipeline
- `app/src/main/java/com/qalqon/security/alert/` - full-screen warning flow
- `app/src/main/java/com/qalqon/security/data/` - Room entities/DAO/repository
- `app/src/main/java/com/qalqon/security/ui/` - Compose UI + ViewModel

## Notes

- This app is designed for **offline** operation.
- On Android 11+, broad storage monitoring requires `MANAGE_EXTERNAL_STORAGE` user approval in system settings.
- Some OEM/background restrictions may require users to disable battery optimization manually for continuous monitoring.

## Build

1. Install JDK 17 and set `JAVA_HOME`.
2. Run:
   - Windows: `gradlew.bat :app:assembleDebug`
   - Unix/macOS: `./gradlew :app:assembleDebug`

## Production caveats

- Android Play policy review may restrict `MANAGE_EXTERNAL_STORAGE` for general distribution.
- Source attribution on Android is best-effort; file-path/source and signature checks are used offline.
