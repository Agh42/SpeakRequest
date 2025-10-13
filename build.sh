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

# Default behavior: do not push unless --push is passed
PUSH=false

# Default: use buildx if available. Can be overridden with --no-buildx or the
# NO_BUILDX environment variable (set to 1 or true).
NO_BUILDX=${NO_BUILDX:-false}

show_help() {
		cat <<EOF
Usage: $(basename "$0") [--push] [--help]

Options:
	--push        Push the built Docker image to Docker Hub. By default the script will build the image but NOT push it.
	--no-buildx   Do not use docker buildx / BuildKit even if available; fall back to classic docker build.
	--help, -h    Show this help message and exit.

Environment variables:
	DOCKER_REPO   Docker repository (default: ${DOCKER_REPO})
	DOCKER_TAG    Docker tag (default: ${DOCKER_TAG})
	DOCKER_USERNAME / DOCKER_PASSWORD
								Optional credentials for non-interactive docker login.

Examples:
	${0}                 # build only, do not push
	${0} --push          # build and push to Docker Hub
	DOCKER_TAG=1.2.3 ${0} --push
EOF
}

# Parse command-line arguments
for arg in "$@"; do
		case "$arg" in
				--push)
						PUSH=true
						;;
			--no-buildx)
				NO_BUILDX=true
				;;
				--help|-h)
						show_help
						exit 0
						;;
				*)
						echo "Unknown option: $arg"
						show_help
						exit 2
						;;
		esac
done

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
BUILD_CACHE_DIR="${BUILD_CACHE_DIR:-./.docker-cache}"

# Ensure buildx is available and create a builder instance if needed
if [ "${NO_BUILDX}" = "true" ] || [ "${NO_BUILDX}" = "1" ]; then
	echo "NO_BUILDX set; forcing classic docker build (no buildx)"
	echo "Using classic docker build (no buildx)."
	docker build --network=host --pull -t "${IMAGE}" .
elif docker buildx version >/dev/null 2>&1; then
	echo "Using docker buildx with BuildKit caching (cache dir: ${BUILD_CACHE_DIR})"

	# Create a named builder if one doesn't exist (idempotent)
	BUILDER_NAME="speakrequest-builder"
	if ! docker buildx inspect "${BUILDER_NAME}" >/dev/null 2>&1; then
		echo "Creating buildx builder '${BUILDER_NAME}'..."
		docker buildx create --name "${BUILDER_NAME}" --use || true
	else
		docker buildx use "${BUILDER_NAME}" || true
	fi

	# Build with buildx. For local development use --load to load the image into local docker.
	# Use a local cache directory to persist layer and Gradle cache between builds.
	docker buildx build --pull --network=host \
		--cache-to=type=local,dest="${BUILD_CACHE_DIR}" \
		--cache-from=type=local,src="${BUILD_CACHE_DIR}" \
		--load -t "${IMAGE}" .
else
	echo "docker buildx not available; falling back to regular docker build (no BuildKit cache mounts)"
	docker build --network=host --pull -t "${IMAGE}" .
fi

echo "Docker image built: ${IMAGE}"

if [ "$PUSH" = true ]; then
	echo "Pushing ${IMAGE} to Docker Hub..."
	docker push "${IMAGE}"
	echo "Push complete. Image available at docker.io/${DOCKER_REPO}:${DOCKER_TAG}"
else
	echo "--push not given; skipping 'docker push'. If you want to push the image, re-run with --push."
fi