@echo off
set PATH=C:\Program Files\nodejs;%APPDATA%\npm;%PATH%
echo ===================================================================
echo  DEPLOYING ASSETS TO CLOUDFLARE PAGES PRODUCTION (h03-themeapp-assets)
echo ===================================================================
wrangler.cmd pages deploy app\src\main\assets --project-name=h03-themeapp-assets --branch=main --commit-dirty=true
echo.
echo ===================================================================
echo  DEPLOY COMPLETE! URL: https://h03-themeapp-assets.pages.dev
echo ===================================================================
pause
