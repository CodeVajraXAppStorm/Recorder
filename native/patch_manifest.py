import sys
p = sys.argv[1]
s = open(p, encoding="utf-8").read()
perms = """    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
"""
comps = """        <receiver android:name=".ReminderReceiver" android:exported="false" />
        <receiver android:name=".BootReceiver" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
            </intent-filter>
        </receiver>
"""
assert "<application" in s and "</application>" in s
s = s.replace("<application", perms + "    <application", 1)
s = s.replace("</application>", comps + "    </application>", 1)
open(p, "w", encoding="utf-8").write(s)
print("manifest patched")
