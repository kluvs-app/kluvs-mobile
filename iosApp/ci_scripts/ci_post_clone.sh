#!/bin/sh
set -e
set -x

echo "🚀 Starting post-cloning task for KMP build"

# Xcode Cloud VMs have been dropping connections at random since 2026-11-16 (lost packets,
# "No route to host", read timeouts on dl.google.com / Maven Central / GitHub). Each download
# fails rarely, but a fresh VM downloads hundreds of them, so some download fails on every run.
# Everything below makes the script ride through that instead of dying on the first drop.

# Heartbeat: Xcode Cloud kills a script that prints nothing for 15 minutes, which is what
# killed the earlier attempt at Gradle retries. Print a line every minute while we run.
( while true; do sleep 60; echo "⏱️ ci_post_clone still running ($(date -u +%H:%M:%S) UTC)"; done ) &
HEARTBEAT_PID=$!
trap 'kill $HEARTBEAT_PID 2>/dev/null || true' EXIT

# The VM's NIC has MTU 1280 but the tunnel carrying the default route claims 1500, so
# full-size packets can be silently dropped. Clamp the tunnel to 1280 if we're allowed to.
DEFAULT_IF=$(route -n get default 2>/dev/null | awk '/interface:/{print $2}' || true)
echo "🌐 Default route interface: ${DEFAULT_IF:-unknown}"
if [ -n "$DEFAULT_IF" ] && sudo -n true 2>/dev/null; then
    sudo -n ifconfig "$DEFAULT_IF" mtu 1280 && echo "🌐 Set $DEFAULT_IF MTU to 1280" || echo "⚠️ Could not change $DEFAULT_IF MTU"
else
    echo "ℹ️ No passwordless sudo; leaving MTU unchanged"
fi

# Fail a stalled download fast (20s) and let Gradle retry it a few times with short backoff
# (0.5s, 1s, 2s, 4s, 8s). The earlier attempt used 10 retries from 2s, which slept silently
# for many minutes.
GRADLE_NET_OPTS="-Dorg.gradle.internal.http.connectionTimeout=20000 -Dorg.gradle.internal.http.socketTimeout=20000 -Dorg.gradle.internal.repository.max.retries=5 -Dorg.gradle.internal.repository.initial.backoff=500"
GRADLE_MAX_ATTEMPTS=5

# Run a Gradle command, re-running it (up to GRADLE_MAX_ATTEMPTS times) only when it failed on
# a network error. Gradle keeps everything it already downloaded, so each attempt only fetches
# what's still missing. Non-network failures (compile errors, test failures) are not retried.
run_gradle() {
    attempt=1
    while :; do
        echo "▶️ ./gradlew $* (attempt $attempt/$GRADLE_MAX_ATTEMPTS)"
        log_file=$(mktemp)
        status_file=$(mktemp)
        { ./gradlew --console=plain $GRADLE_NET_OPTS "$@" && echo 0 > "$status_file" || echo $? > "$status_file"; } 2>&1 | tee "$log_file"
        status=$(cat "$status_file")
        if [ "$status" = "0" ]; then
            rm -f "$log_file" "$status_file"
            return 0
        fi
        if ! grep -Eq 'Could not (GET|HEAD|download|resolve)|No route to host|timed out|Connection reset|Got socket exception|Remote host terminated|UnknownHostException' "$log_file"; then
            echo "❌ ./gradlew $* failed for a non-network reason; not retrying"
            rm -f "$log_file" "$status_file"
            return "$status"
        fi
        rm -f "$log_file" "$status_file"
        if [ "$attempt" -ge "$GRADLE_MAX_ATTEMPTS" ]; then
            echo "❌ ./gradlew $* still failing on network errors after $attempt attempts"
            return "$status"
        fi
        attempt=$((attempt + 1))
        echo "🔁 Network failure; retrying in 15s"
        sleep 15
    done
}

# 1. Install Java 17 (Skip auto-update to save time)
HOMEBREW_NO_AUTO_UPDATE=1 brew install openjdk@17

# 2. Set JAVA_HOME using the brew prefix (Avoids sudo/symlinks)
export JAVA_HOME=$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"

# 3. Verify Java
echo "✅ Java version installed:"
java -version

# 4. Fix Gradle Permissions (Moving up two levels from iosApp/ci_scripts)
cd ../..
if [ -f "./gradlew" ]; then
    echo "✅ Found gradlew at $(pwd). Setting permissions..."
    chmod +x gradlew
else
    echo "❌ ERROR: gradlew not found! Current dir is: $(pwd)"
    ls -F
    exit 1
fi

# 5. SETUP SENTRY (Must happen first!)
# This ensures the .xcframework is downloaded and ready for the test linking
echo "⬇️ Fetching Sentry Framework..."
run_gradle setupSentryForCi

# 6. RUN TESTS
# We run this here to gate the build. If this fails, the build stops.
# Explicit -Pbuildkonfig.flavor=integration: Xcode Cloud VMs are ephemeral (no persisted
# gradle.properties), so this can't rely on a local default the way `./gradlew` alone can.
echo "🧪 Running KMP Simulator Tests..."
run_gradle iosSimulatorArm64Test '-PexcludeTests=**/*IntegrationTest*' -Pbuildkonfig.flavor=integration --continue

# 7. PREFETCH DEVICE DEPENDENCIES FOR ARCHIVES
# The "Compile Kotlin Framework" build phase runs Gradle again inside xcodebuild, without these
# retries. For archives it builds for devices (iosArm64), whose libraries the simulator tests
# above never downloaded, so compile that target here while retries are available.
if [ "$CI_XCODEBUILD_ACTION" = "archive" ]; then
    echo "📦 Prefetching iosArm64 dependencies for archive..."
    run_gradle :shared:compileKotlinIosArm64 -Pbuildkonfig.flavor=production
fi

echo "✅ Post-clone complete. Tests Passed. Environment ready for Xcode build. 🏁"
