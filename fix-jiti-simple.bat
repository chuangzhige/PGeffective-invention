@echo off
echo Fixing jiti compatibility issue...
echo.

cd /d "cz-admin-master"

echo Step 1: Clearing dependencies...
rmdir /s /q node_modules
del pnpm-lock.yaml

echo Step 2: Installing compatible jiti version...
pnpm add jiti@^1.21.0 --save-dev

echo Step 3: Reinstalling all dependencies...
pnpm install

echo Step 4: Verifying jiti version...
pnpm list jiti

echo.
echo Fix completed! Now try: pnpm dev
pause 