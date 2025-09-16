#!/usr/bin/env bash
set -e

# Version of Gradle to install wrapper for
GRADLE_VERSION=8.9

echo "Bootstrapping Gradle wrapper (version $GRADLE_VERSION)..."
gradle wrapper --gradle-version "$GRADLE_VERSION"

echo "Wrapper created. Use ./gradlew from now on."