#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

mkdir -p build/main
find src/main -name '*.java' -print0 | xargs -0 javac -d build/main

export PORT="${PORT:-8080}"
exec java -cp build/main notes.NotesApp