#!/usr/bin/env bash
set -euo pipefail

# Script to build datapath.aar independently using gomobile
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
DATAPATH_DIR="$ROOT_DIR/datapath"
OUTPUT_DIR="$ROOT_DIR/app/build/gomobile"
LIBS_DIR="$ROOT_DIR/app/libs"

mkdir -p "$OUTPUT_DIR" "$LIBS_DIR"

echo "Checking Go installation..."
if ! command -v go &> /dev/null; then
    echo "Error: Go is not installed. Please install Go from https://go.dev/dl/"
    exit 1
fi

echo "Checking gomobile..."
if ! command -v gomobile &> /dev/null; then
    if [ -f "$HOME/go/bin/gomobile" ]; then
        export PATH="$HOME/go/bin:$PATH"
    else
        echo "Installing gomobile..."
        go install golang.org/x/mobile/cmd/gomobile@latest
        export PATH="$HOME/go/bin:$PATH"
        gomobile init
    fi
fi

echo "Building datapath.aar for android/arm64..."
cd "$DATAPATH_DIR"
gomobile bind -target=android/arm64 -androidapi 24 -o "$OUTPUT_DIR/datapath.aar" .

# Also copy to app/libs as persistent backup
cp "$OUTPUT_DIR/datapath.aar" "$LIBS_DIR/datapath.aar"

echo "Successfully built datapath.aar at:"
echo "  - $OUTPUT_DIR/datapath.aar"
echo "  - $LIBS_DIR/datapath.aar"
