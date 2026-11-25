#!/bin/bash
# Undo install.sh (used automatically if the native layer fails to compile).
SRC=android/app/src/main/java/com/namya/app
M=android/app/src/main/AndroidManifest.xml
[ -f "$SRC/MainActivity.java.orig" ] && mv "$SRC/MainActivity.java.orig" "$SRC/MainActivity.java"
[ -f "$M.orig" ] && mv "$M.orig" "$M"
rm -f "$SRC/OverlayPopupPlugin.java" "$SRC/PopupCore.java" "$SRC/ReminderReceiver.java" "$SRC/PopupOverlay.java" "$SRC/BootReceiver.java"
echo "native popup layer removed"
exit 0
