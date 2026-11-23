@echo off
cd /d "%~dp0"
if not exist out mkdir out
javac -d out src\*.java
echo ===== DEMO =====
java -cp out K2703397_Demo
echo.
echo ===== TEST CASES =====
java -cp out K2703397_TestRunner
pause
