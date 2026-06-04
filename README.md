# ZERO-fications: Advanced Notification Blocker for Android

Remove, block, and silence any notification on Android — including ongoing (persistent) ones that normally can't be dismissed.

---

## How It Works

Android does not allow apps to cancel ongoing notifications. This app works around that by **snoozing them for 100 years**, which makes them disappear from the status bar indefinitely. You can restore any notification at any time.

---

## Features

### Manual Hide / Show
- View all notifications (active and hidden) in a single list
- **Check the checkbox** on any notification to hide it instantly
- Uncheck to bring it back
- Pull down to refresh the list

### Per-App Block Rules
- **Long-press** any notification and select "Block" to permanently auto-hide all future notifications from that app
- New notifications from blocked apps are silenced the moment they appear — no manual action needed

### Keyword Rules
- Go to **Block Rules** (toolbar icon) and tap **+** to add a keyword
- Any notification whose title or text contains that keyword will be auto-hidden

### Kill Switch
- Tap the **silence FAB** (bottom-right button) to instantly hide every notification on your device at once
- Choose how long: **30 min / 1 hour / 2 hours / 4 hours / Until I turn it off**
- A banner shows at the top when the kill switch is active with the exact time it expires
- For timed durations, notifications return automatically when time is up — no action needed
- Tap **Turn Off** in the banner (or the FAB again) to restore everything early

### Rules Management Screen
- Tap the **manage icon** in the toolbar to open the Block Rules screen
- See all active per-app and keyword rules in one list
- Delete any rule with the trash button

---

## Installation

1. Clone or download this repository
2. Open in Android Studio
3. Build and install on your device (minSdk 29 / Android 10+)
4. On first launch, tap **Enable** to grant Notification Listener permission
5. That's it — the service starts automatically when you open the app and stops when you close it

---

## Privacy

- No internet permission
- No data leaves your device
- No ads, no analytics, no accounts
- Rules and kill switch state are stored locally in SharedPreferences only

---

## Requirements

- Android 10 (API 29) or higher
- Notification Listener Service permission (prompted on first launch)

---

## Package

`com.mahir.notification_remover`
