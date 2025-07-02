@echo off
echo Complete Fix for Node.js Multi-Version Issues
echo =============================================
echo.

cd /d "D:\Admin\CZwebsite-admin\cz-admin-master"

echo Step 1: Cleaning pnpm store and cache...
pnpm store prune --force
pnpm cache clean --force

echo Step 2: Removing problematic node_modules...
if exist node_modules rmdir /s /q node_modules
if exist .pnpm-workspace-state.json del .pnpm-workspace-state.json
if exist pnpm-lock.yaml del pnpm-lock.yaml

echo Step 3: Cleaning internal packages...
if exist internal\vite-config\node_modules rmdir /s /q internal\vite-config\node_modules
if exist internal\vite-config\dist rmdir /s /q internal\vite-config\dist
if exist internal\eslint-config\node_modules rmdir /s /q internal\eslint-config\node_modules
if exist internal\eslint-config\dist rmdir /s /q internal\eslint-config\dist
if exist internal\stylelint-config\node_modules rmdir /s /q internal\stylelint-config\node_modules
if exist internal\stylelint-config\dist rmdir /s /q internal\stylelint-config\dist
if exist internal\ts-config\node_modules rmdir /s /q internal\ts-config\node_modules
if exist internal\ts-config\dist rmdir /s /q internal\ts-config\dist

echo Step 4: Installing compatible jiti version...
pnpm add jiti@1.21.7 --save-dev

echo Step 5: Fresh install of all dependencies...
pnpm install --frozen-lockfile=false

echo Step 6: Building internal packages...
pnpm run postinstall

echo Step 7: Verifying installation...
pnpm list jiti
echo.

echo Step 8: Testing configuration...
pnpm run type:check

echo.
echo =============================================
echo Fix completed! Now try: pnpm dev
echo If still fails, we'll use a native vite config
pause 