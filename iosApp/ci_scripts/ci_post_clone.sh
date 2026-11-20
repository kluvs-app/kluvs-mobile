#!/bin/sh
set -e
set -x

echo "🚀 Starting post-cloning task for KMP build"

# Heartbeat: Xcode Cloud kills a script that prints nothing for 15 minutes, and Gradle can stay
# silent that long while it resolves dependencies. Print a line every minute while we run.
( set +x; while true; do sleep 60; echo "⏱️ ci_post_clone still running ($(date -u +%H:%M:%S) UTC)"; done ) &
HEARTBEAT_PID=$!
trap 'kill $HEARTBEAT_PID 2>/dev/null || true' EXIT

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
./gradlew --console=plain setupSentryForCi

# 6. RUN TESTS
# We run this here to gate the build. If this fails, the build stops.
# Explicit -Pbuildkonfig.flavor=integration: Xcode Cloud VMs are ephemeral (no persisted
# gradle.properties), so this can't rely on a local default the way `./gradlew` alone can.
echo "🧪 Running KMP Simulator Tests..."
./gradlew --console=plain iosSimulatorArm64Test '-PexcludeTests=**/*IntegrationTest*' -Pbuildkonfig.flavor=integration --continue

echo "✅ Post-clone complete. Tests Passed. Environment ready for Xcode build. 🏁"
