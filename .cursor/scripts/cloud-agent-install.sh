#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../" && pwd)"
cd "$ROOT_DIR"

: "${ANDROID_HOME:=/opt/android-sdk}"
: "${JAVA_HOME:=/usr/lib/jvm/java-17-openjdk-amd64}"

export ANDROID_HOME JAVA_HOME
export PATH="${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/emulator:${PATH}"

CMDTOOLS_ZIP="/tmp/android-cmdline-tools.zip"
CMDTOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip"

if [[ ! -x "${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager" ]]; then
  mkdir -p "${ANDROID_HOME}/cmdline-tools"
  curl -fsSL "${CMDTOOLS_URL}" -o "${CMDTOOLS_ZIP}"
  unzip -qo "${CMDTOOLS_ZIP}" -d /tmp/android-cmdline-tools
  rm -rf "${ANDROID_HOME}/cmdline-tools/latest"
  mv /tmp/android-cmdline-tools/cmdline-tools "${ANDROID_HOME}/cmdline-tools/latest"
  rm -rf /tmp/android-cmdline-tools "${CMDTOOLS_ZIP}"
fi

yes | sdkmanager --licenses >/dev/null

sdkmanager --install \
  "platform-tools" \
  "emulator" \
  "platforms;android-35" \
  "build-tools;35.0.0" \
  "system-images;android-34;google_apis;x86_64"

if ! avdmanager list avd 2>/dev/null | grep -q "Name: ridecast_avd"; then
  echo "no" | avdmanager create avd \
    -n ridecast_avd \
    -k "system-images;android-34;google_apis;x86_64" \
    -d pixel_6 \
    --force
fi

# local.properties from environment secrets when available; otherwise use placeholders for compile-only builds.
if [[ ! -f local.properties ]]; then
  SDK_DIR="${ANDROID_HOME}"
  MAPS_KEY="${MAPS_API_KEY:-placeholder_maps_key}"
  WEATHER_KEY="${WEATHER_API_KEY:-placeholder_weather_key}"
  cat > local.properties <<EOF
sdk.dir=${SDK_DIR}
MAPS_API_KEY=${MAPS_KEY}
WEATHER_API_KEY=${WEATHER_KEY}
EOF
fi

./gradlew assembleDebug --no-daemon
