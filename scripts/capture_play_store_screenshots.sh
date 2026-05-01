#!/usr/bin/env bash
# Capture six Play Store phone screenshots (one per bottom-nav tab) via adb.
#
# Prerequisites:
#   - USB debugging enabled (or emulator running), `adb devices` shows "device"
#   - Debug or release build installed: ./gradlew :app:installDebug
#   - Turn OFF biometric lock in Settings (otherwise the gate blocks automation)
#
# Usage:
#   ./scripts/capture_play_store_screenshots.sh
#   SCREENSHOT_DELAY=3 ./scripts/capture_play_store_screenshots.sh   # slower devices

set -euo pipefail

PACKAGE="com.michael.insightlyspend"
ACTIVITY="com.michael.insightlyspend/.MainActivity"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="${ROOT}/play-assets/screenshots"

# Resolve adb: PATH, then Android Studio default SDK (macOS/Linux), then ANDROID_HOME / ANDROID_SDK_ROOT.
resolve_adb() {
  local candidates=()
  [[ -n "${ADB:-}" ]] && candidates+=("$ADB")
  command -v adb >/dev/null 2>&1 && candidates+=("$(command -v adb)")
  [[ -n "${ANDROID_HOME:-}" ]] && candidates+=("${ANDROID_HOME}/platform-tools/adb")
  [[ -n "${ANDROID_SDK_ROOT:-}" ]] && candidates+=("${ANDROID_SDK_ROOT}/platform-tools/adb")
  candidates+=("${HOME}/Library/Android/sdk/platform-tools/adb")
  candidates+=("${HOME}/Android/Sdk/platform-tools/adb")

  local p
  for p in "${candidates[@]}"; do
    [[ -x "$p" ]] && { echo "$p"; return 0; }
  done
  return 1
}

ADB_BIN="$(resolve_adb)" || {
  echo "adb not found." >&2
  echo "Install Android SDK Platform-Tools, or set ANDROID_HOME (or ANDROID_SDK_ROOT) to your SDK path." >&2
  echo "macOS (Android Studio): SDK is usually ~/Library/Android/sdk — add to PATH:" >&2
  echo "  export PATH=\"\$HOME/Library/Android/sdk/platform-tools:\$PATH\"" >&2
  exit 1
}

if ! "$ADB_BIN" shell echo ok >/dev/null 2>&1; then
  echo "adb cannot reach a device/emulator. Run: \"$ADB_BIN\" devices" >&2
  exit 1
fi

echo "Using adb: $ADB_BIN" >&2
echo "Tip: disable biometric lock in the app before running." >&2
mkdir -p "$OUT"

# Matches bottom bar order: Home, Ledger, Insights, Budget, Vault, Settings
ROUTES=(dashboard ledger analytics budget receipts settings)
NAMES=(01_home 02_ledger 03_insights 04_budget 05_vault 06_settings)

DELAY="${SCREENSHOT_DELAY:-2.5}"

for i in "${!ROUTES[@]}"; do
  route="${ROUTES[$i]}"
  name="${NAMES[$i]}"
  "$ADB_BIN" shell am force-stop "$PACKAGE" 2>/dev/null || true
  sleep 0.35
  "$ADB_BIN" shell am start -a android.intent.action.VIEW \
    -d "insightlyspend://nav/${route}" \
    -n "$ACTIVITY" \
    -f 0x10000000
  sleep "$DELAY"
  "$ADB_BIN" exec-out screencap -p > "${OUT}/${name}.png"
  echo "Wrote ${OUT}/${name}.png"
done

echo "Done. Upload play-assets/screenshots/*.png under Phone screenshots in Play Console."
