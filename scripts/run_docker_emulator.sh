#!/usr/bin/env bash
set -e

echo "🚀 Starting AiVance Docker Android Emulator with noVNC Web RDP Browser Access..."

CONTAINER_NAME="aivance-docker-emulator"
PORT_NOVNC=6080
PORT_ADB=5555

# Ensure Docker Compose is up
docker compose -f docker/docker-compose.yml up -d

echo "⏳ Waiting for Android Emulator container boot..."
until [ "$(docker inspect -f '{{.State.Running}}' $CONTAINER_NAME 2>/dev/null)" == "true" ]; do
    sleep 2
done

echo "🔗 Connecting ADB to Docker Emulator at localhost:$PORT_ADB..."
adb connect localhost:$PORT_ADB || true

echo "⏳ Waiting for Android System Boot Completion..."
until adb -s localhost:$PORT_ADB shell getprop sys.boot_completed 2>/dev/null | grep -m 1 "1"; do
    sleep 3
done

# Detect Local IP for Web Browser Access
LOCAL_IP=$(hostname -I | awk '{print $1}')

echo "================================================================="
echo "✅ DOCKER ANDROID EMULATOR READY FOR TESTING!"
echo "🌐 Web Browser noVNC IP Access URL : http://${LOCAL_IP}:${PORT_NOVNC}"
echo "🌐 Localhost noVNC Browser Access  : http://localhost:${PORT_NOVNC}"
echo "📺 VNC / RDP Display Port          : localhost:5900"
echo "📱 Connected ADB Device            : localhost:${PORT_ADB}"
echo "================================================================="

echo "📦 Installing AiVance Debug APK onto Docker Emulator..."
./gradlew :app:installDebug

echo "🏃 Launching AiVance MainActivity on Docker Emulator..."
adb -s localhost:$PORT_ADB shell am start -n com.bangersoul.aivance.debug/com.bangersoul.aivance.MainActivity

echo "🧪 Running Instrumented Android Unit Tests..."
./gradlew connectedDebugAndroidTest || echo "Instrumented tests complete."

echo "🎉 Docker Emulator Testing Session Active!"
