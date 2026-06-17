#!/usr/bin/env bash
set -euo pipefail

usage() {
  echo "Usage: $0 <version> [-y]"
  echo "  <version>  New version, e.g. 1.0.1"
  echo "  -y         Auto-commit, tag, and push"
  exit 1
}

VERSION=""
AUTO_PUSH=false

for arg in "$@"; do
  case "$arg" in
    -y) AUTO_PUSH=true ;;
    -*) usage ;;
    *)  VERSION="$arg" ;;
  esac
done

[ -z "$VERSION" ] && usage

if ! echo "$VERSION" | grep -qE '^[0-9]+\.[0-9]+\.[0-9]+$'; then
  echo "Error: version must be in x.y.z format (e.g. 1.0.1)"
  exit 1
fi

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROPS="${REPO_ROOT}/gradle.properties"

# Update mod_version in gradle.properties
sed -i.bak -E "s/^mod_version=.*/mod_version=${VERSION}/" "$PROPS"
rm "${PROPS}.bak"

echo "Version bumped to ${VERSION}"
echo "Updated: gradle.properties"

if [ "$AUTO_PUSH" = true ]; then
  cd "$REPO_ROOT"
  git add gradle.properties
  git commit -m "chore: bump version to ${VERSION}"
  git tag "v${VERSION}"
  git push
  git push origin "v${VERSION}"
  echo "Committed, tagged v${VERSION}, and pushed."
fi
