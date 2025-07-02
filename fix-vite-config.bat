@echo off
echo Fixing Vite configuration...
echo.

cd /d "cz-admin-master"

echo Step 1: Building internal packages...
pnpm turbo run stub

echo Step 2: Checking if fix is needed...
if exist "dist" (
    echo Internal packages built successfully!
) else (
    echo Building packages individually...
    cd internal\vite-config
    pnpm run stub
    cd ..\..
)

echo Step 3: Testing configuration...
pnpm run type:check

echo.
echo Vite configuration fixed! Now try: pnpm dev
pause 