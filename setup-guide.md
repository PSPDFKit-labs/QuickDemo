***QuickDemo is a tool for Android developers. If you don't know what this application is for, do not use it!***

## Setup

To enable _QuickDemo_ you need to do two things.

1. Enable the System UI demo mode on your phone.
2. Grant the `android.permission.DUMP` to the QuickDemo app. This permissions is protected and can't be requested at runtime.

Both steps can be performed from you command line via `adb`.

```
# Ensure demo mode is activated.
adb shell settings put global sysui_demo_allowed 1

# Grant required permissions.
adb shell pm grant com.pspdfkit.labs.quickdemo android.permission.DUMP
```

**Please note:** The permissions is granted until you revoke it or uninstall the app.