@echo off
echo Fixing jiti compatibility issue...
echo.

:: Navigate to frontend directory
cd /d "cz-admin-master"
if %errorlevel% neq 0 (
    echo Cannot find cz-admin-master directory
    pause
    exit /b 1
)

echo Step 1: Removing jiti 2.x from package.json...
:: Remove jiti 2.4.2 from dependencies
powershell -Command "(Get-Content package.json) -replace '\"jiti\": \"\^2\.4\.2\",' '' | Set-Content package.json"

echo Step 2: Installing compatible jiti version...
:: Install jiti 1.x version that's compatible with Vite 5.x
pnpm add jiti@^1.21.0

echo Step 3: Clearing node_modules and lock file...
:: Clean install to resolve conflicts
pnpm store prune
rmdir /s /q node_modules
del pnpm-lock.yaml

echo Step 4: Reinstalling dependencies...
pnpm install

echo Step 5: Verifying installation...
pnpm list jiti

echo.
echo Fix completed! Try starting the frontend now.
pause 