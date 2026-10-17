#!/usr/bin/env bash
# ==============================================================================
# SZM Vitalis — Deterministic Release Candidate Signing Utility (szm / Team Beyond Horizon)
# ==============================================================================
set -euo pipefail

KEYSTORE_PATH="${1:-}"
KEY_ALIAS="${2:-}"

if [[ -z "$KEYSTORE_PATH" || -z "$KEY_ALIAS" ]]; then
    echo "Usage: ./generate_signed_release.sh <path_to_keystore.jks> <key_alias>"
    echo "Note: Never check production private keystores or passwords into Git."
    exit 1
fi

echo "==> Building Release APK..."
./gradlew :app:assembleRelease

UNSIGNED_APK="app/build/outputs/apk/release/app-release-unsigned.apk"
SIGNED_APK="app/build/outputs/apk/release/app-release-signed.apk"

if [[ ! -f "$UNSIGNED_APK" ]]; then
    echo "Error: Unsigned release APK not found at $UNSIGNED_APK"
    exit 1
fi

echo "==> Signing APK using apksigner..."
# Using Android build-tools apksigner
APKSIGNER=$(find "${ANDROID_HOME:-$HOME/Android/Sdk}"/build-tools/ -name apksigner | sort -V | tail -n 1)

if [[ -z "$APKSIGNER" ]]; then
    echo "Error: apksigner not found in Android SDK build-tools."
    exit 1
fi

"$APKSIGNER" sign \
    --ks "$KEYSTORE_PATH" \
    --ks-key-alias "$KEY_ALIAS" \
    --out "$SIGNED_APK" \
    "$UNSIGNED_APK"

echo "==> Verifying signature..."
"$APKSIGNER" verify --verbose "$SIGNED_APK"

echo "==> Signature verified successfully."
echo "Signed APK ready at: $SIGNED_APK"
sha256sum "$SIGNED_APK"
