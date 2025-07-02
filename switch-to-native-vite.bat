@echo off
echo Switching to Native Vite Configuration...
echo ==========================================
echo.

cd /d "D:\Admin\CZwebsite-admin\cz-admin-master"

echo Step 1: Backup current vite.config.ts...
if exist vite.config.ts (
    copy vite.config.ts vite.config.backup.ts
    echo Current config backed up as vite.config.backup.ts
)

echo Step 2: Installing required Vite plugins...
pnpm add @vitejs/plugin-vue --save-dev

echo Step 3: Switch to native configuration...
if exist vite.config.native.ts (
    copy vite.config.native.ts vite.config.ts
    echo Switched to native Vite configuration
) else (
    echo Error: vite.config.native.ts not found!
    pause
    exit /b 1
)

echo Step 4: Test the new configuration...
pnpm run type:check

echo.
echo ==========================================
echo Native Vite config activated!
echo Now try: pnpm dev
echo.
echo To restore original config:
echo copy vite.config.backup.ts vite.config.ts
pause 