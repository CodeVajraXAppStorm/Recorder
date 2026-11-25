#!/bin/bash
# Namya: real popup layer (native Android). Run from the repo root after "cap add android".
set -e
SRC=android/app/src/main/java/com/namya/app
M=android/app/src/main/AndroidManifest.xml
[ -f "$SRC/MainActivity.java" ] || { echo "MainActivity.java not found"; exit 1; }
cp "$SRC/MainActivity.java" "$SRC/MainActivity.java.orig"
cp "$M" "$M.orig"
cp native/*.java "$SRC/"
python3 native/patch_manifest.py "$M"
echo "native popup layer installed"
