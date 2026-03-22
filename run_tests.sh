#!/bin/bash
# Run unit tests for Traffic Simulator
# Usage: ./run_tests.sh  (from the project root)

set -e
cd "$(dirname "$0")"

JUNIT_JAR="lib/junit-platform-console-standalone-1.10.0.jar"
PROD_CLASSES="bin"
STUBS="bin/stubs"
TEST_OUT="bin/test"
TEST_SRC="src/test/main"

mkdir -p "$TEST_OUT"

echo "=== Compiling tests ==="
javac -cp "$JUNIT_JAR:$PROD_CLASSES:$STUBS" \
      -d "$TEST_OUT" \
      "$TEST_SRC"/NodeTest.java \
      "$TEST_SRC"/BucketTest.java \
      "$TEST_SRC"/BucketsetTest.java \
      "$TEST_SRC"/RouteTest.java \
      "$TEST_SRC"/WaypointsTest.java

echo "=== Running tests ==="
java -jar "$JUNIT_JAR" \
     --class-path "$TEST_OUT:$PROD_CLASSES:$STUBS" \
     --scan-class-path
