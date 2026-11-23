#!/bin/sh
cd "$(dirname "$0")"
mkdir -p out && javac -d out src/*.java
echo "===== DEMO =====" && java -cp out K2703397_Demo
echo "===== TEST CASES =====" && java -cp out K2703397_TestRunner
