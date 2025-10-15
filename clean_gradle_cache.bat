@echo off
echo ========================================
echo Cleaning Gradle Cache for BambooTrace
echo ========================================
echo.

echo [1/3] Stopping Gradle Daemon...
call gradlew.bat --stop
echo.

echo [2/3] Cleaning project .gradle folder...
if exist ".gradle" (
    rmdir /s /q ".gradle"
    echo     Deleted: .gradle
) else (
    echo     Already clean
)
echo.

echo [3/3] Cleaning app build folder...
if exist "app\build" (
    rmdir /s /q "app\build"
    echo     Deleted: app\build
) else (
    echo     Already clean
)
echo.

echo ========================================
echo Cleanup Complete!
echo ========================================
echo.
echo Next steps:
echo 1. Open Android Studio
echo 2. File ^> Invalidate Caches... ^> Invalidate and Restart
echo 3. After restart, sync project with Gradle Files
echo 4. Build ^> Clean Project
echo 5. Build ^> Rebuild Project
echo 6. Click Run button to launch app
echo.
echo Note: First build may take 5-15 minutes (downloading dependencies)
echo.
echo For detailed instructions, see: BUILD_FIX_GUIDE.md
echo.
pause
