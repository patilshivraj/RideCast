#!/usr/bin/env bash
set -euo pipefail

: "${ANDROID_HOME:=/opt/android-sdk}"
export PATH="${ANDROID_HOME}/emulator:${ANDROID_HOME}/platform-tools:${PATH}"

# Nested virtualization can hang with KVM; software emulation is slower but reliable here.
if pgrep -f "qemu-system-x86_64.*ridecast_avd" >/dev/null 2>&1; then
  exit 0
fi

emulator -avd ridecast_avd \
  -no-window \
  -no-audio \
  -no-boot-anim \
  -gpu swiftshader_indirect \
  -accel off \
  -no-snapshot-save \
  >/tmp/ridecast-emulator.log 2>&1 &

disown
