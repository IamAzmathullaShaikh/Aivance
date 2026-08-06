#!/usr/bin/env bash
# Aivance build helper — sets the toolchain env and runs Gradle.
#
# Usage:
#   ./scripts/build.sh                # assembleDebug
#   ./scripts/build.sh test           # all unit tests
#   ./scripts/build.sh lint           # lintDebug
#   ./scripts/build.sh clean assemble # clean + full assemble
#   ./scripts/build.sh <any gradle args...>
set -euo pipefail

export JAVA_HOME="${JAVA_HOME:-$HOME/.jdks/jdk-21.0.12+8}"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$ANDROID_HOME}"

cd "$(dirname "$0")/.."

case "${1:-assemble}" in
  test)  shift || true; exec ./gradlew testDebugUnitTest "$@" ;;
  lint)  shift || true; exec ./gradlew lintDebug "$@" ;;
  *)     exec ./gradlew "$@" ;;
esac
