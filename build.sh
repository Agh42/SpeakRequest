#!/usr/bin/env bash
set -e

echo "Cleaning and building Spring Boot app..."
./gradlew clean build

echo "Build complete. JAR available in build/libs/"
ls -lh build/libs/*.jar