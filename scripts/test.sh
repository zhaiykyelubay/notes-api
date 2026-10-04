#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

rm -rf build
mkdir -p build/classes
find src -name '*.java' -print0 | xargs -0 javac -d build/classes
java -cp build/classes notes.NotesAppTest