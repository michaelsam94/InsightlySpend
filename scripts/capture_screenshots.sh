#!/usr/bin/env bash
# Back-compat alias for capture_play_store_screenshots.sh
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "${SCRIPT_DIR}/capture_play_store_screenshots.sh" "$@"
