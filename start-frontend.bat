@echo off
echo Starting Frontend Development Server...
echo.

:: Check if pnpm is installed
pnpm --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Installing pnpm globally...
    npm install -g pnpm
    if %errorlevel% neq 0 (
        echo Failed to install pnpm. Please install it manually: npm install -g pnpm
        pause
        exit /b 1
    )
)

:: Navigate to frontend directory
cd /d "cz-admin-master"
if %errorlevel% neq 0 (
    echo Cannot find cz-admin-master directory
    pause
    exit /b 1
)

:: Install dependencies if node_modules doesn't exist or is incomplete
if not exist "node_modules\vite" (
    echo Installing dependencies...
    pnpm install
    if %errorlevel% neq 0 (
        echo Failed to install dependencies
        pause
        exit /b 1
    )
)

:: Start development server
echo Starting development server...
pnpm dev

pause 