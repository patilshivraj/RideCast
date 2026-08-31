#!/usr/bin/env bash
set -euo pipefail

: "${ANDROID_HOME:=/opt/android-sdk}"
export PATH="${ANDROID_HOME}/platform-tools:${PATH}"

TIMEOUT_SECONDS="${EMULATOR_BOOT_TIMEOUT:-900}"
INTERVAL_SECONDS=10
elapsed=0

adb start-server
adb wait-for-device

while (( elapsed < TIMEOUT_SECONDS )); do
  boot_completed="$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')"
  if [[ "${boot_completed}" == "1" ]]; then
  sleep 15
  exit 0
  fi
  sleep "${INTERVAL_SECONDS}"
  elapsed=$((elapsed + INTERVAL_SECONDS))
done

echo "Emulator did not finish booting within ${TIMEOUT_SECONDS}s" >&2
exit 1
