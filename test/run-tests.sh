#!/usr/bin/env bash
# Compila src + test y corre el harness de robustez desde un directorio temporal
# (ScoreRepository escribe "scores.txt" en el CWD).
set -e

HERE="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$HERE/bin-test"
JAVAC="${JAVAC:-javac}"
JAVA="${JAVA:-java}"

rm -rf "$OUT"
mkdir -p "$OUT"

echo ">> compilando..."
find "$HERE/src" "$HERE/test" -name '*.java' > "$OUT/sources.txt"
"$JAVAC" -encoding UTF-8 -d "$OUT" @"$OUT/sources.txt"

RUNDIR="$(mktemp -d)"
echo ">> corriendo tests en $RUNDIR"
cd "$RUNDIR"
set +e
"$JAVA" -Dfile.encoding=UTF-8 -cp "$OUT" tests.TestMain
CODE=$?
set -e
cd "$HERE"
rm -rf "$RUNDIR"
exit $CODE
