#!/bin/sh
# Runs after every xcodebuild action (build, test, archive). On failure, mirror the failing
# tests onto the PR (see report_failure.swift). Never changes the build's outcome.

[ "${CI_XCODEBUILD_EXIT_CODE:-0}" = "0" ] && exit 0

SCRIPTS_DIR=$(cd "$(dirname "$0")" && pwd)
if [ -n "$CI_RESULT_BUNDLE_PATH" ] && [ -e "$CI_RESULT_BUNDLE_PATH" ]; then
    xcrun swift "$SCRIPTS_DIR/report_failure.swift" --stage "xcodebuild ${CI_XCODEBUILD_ACTION:-?}" --xcresult "$CI_RESULT_BUNDLE_PATH" || true
else
    xcrun swift "$SCRIPTS_DIR/report_failure.swift" --stage "xcodebuild ${CI_XCODEBUILD_ACTION:-?}" || true
fi
exit 0
