#!/usr/bin/env bash
set -e

#echo "Cleaning and building Spring Boot app..."
#./gradlew clean build

#echo "Build complete. JAR available in build/libs/"
#ls -lh build/libs/*.jar

# Docker image settings (can be overridden via environment)
DOCKER_REPO="${DOCKER_REPO:-agh42/speakrequest}"
DOCKER_TAG="${DOCKER_TAG:-latest}"
IMAGE="${DOCKER_REPO}:${DOCKER_TAG}"

echo "\nPreparing to build Docker image: ${IMAGE}"

# Ensure Docker CLI is available
if ! command -v docker >/dev/null 2>&1; then
	echo "Docker CLI not found. Please install Docker to continue."
	exit 1
fi

# Ensure Docker daemon is accessible
if ! docker info >/dev/null 2>&1; then
	echo "Docker daemon is not running or you don't have permission to access it."
	echo "Start Docker and ensure your user can access the docker daemon (or run this script with sudo)."
	exit 1
fi

# If credentials provided via environment variables, log in non-interactively
if [ -n "${DOCKER_USERNAME:-}" ] && [ -n "${DOCKER_PASSWORD:-}" ]; then
	echo "Logging in to Docker Hub as ${DOCKER_USERNAME} (from environment variables)..."
	echo "${DOCKER_PASSWORD}" | docker login -u "${DOCKER_USERNAME}" --password-stdin
else
	echo "No DOCKER_USERNAME/DOCKER_PASSWORD provided. Ensure you're logged in to Docker Hub (docker login) before pushing."
fi

echo "Building Docker image ${IMAGE}..."
docker build --network=host --pull -t "${IMAGE}" .

echo "Docker image built: ${IMAGE}"

echo "Pushing ${IMAGE} to Docker Hub..."
docker push "${IMAGE}"

echo "Push complete. Image available at docker.io/${DOCKER_REPO}:${DOCKER_TAG}"