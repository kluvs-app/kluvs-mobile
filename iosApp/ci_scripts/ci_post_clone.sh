#!/bin/sh
set -e
set -x

echo "🚀 Starting post-cloning task for KMP build"

# Heartbeat: Xcode Cloud kills a script that prints nothing for 15 minutes, and Gradle can stay
# silent that long while it resolves dependencies. Print a line every minute while we run.
( set +x; while true; do sleep 60; echo "⏱️ ci_post_clone still running ($(date -u +%H:%M:%S) UTC)"; done ) &
HEARTBEAT_PID=$!
trap 'kill $HEARTBEAT_PID 2>/dev/null || true' EXIT

# Xcode Cloud's network occasionally stalls a TLS handshake ("Read timed out"), even to the
# dependency mirror configured in settings.gradle.kts. One failed request fails the whole Gradle
# run, so: fail a stalled request fast (15s), let Gradle retry it (6x, 0.5s doubling backoff),
# and if a run still fails on a network error, re-run it. Gradle keeps every file it already
# downloaded, so each attempt only fetches what's still missing.
GRADLE_NET_OPTS="-Dorg.gradle.internal.http.connectionTimeout=15000 -Dorg.gradle.internal.http.socketTimeout=15000 -Dorg.gradle.internal.repository.max.retries=6 -Dorg.gradle.internal.repository.initial.backoff=500"
GRADLE_MAX_ATTEMPTS=6

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
        echo "🔁 Network failure; retrying in 10s"
        sleep 10
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

echo "✅ Post-clone complete. Tests Passed. Environment ready for Xcode build. 🏁"
