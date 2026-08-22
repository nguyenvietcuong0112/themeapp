@echo off
set PATH=C:\Program Files\nodejs;%APPDATA%\npm;%PATH%
echo ===================================================================
echo  DEPLOYING 14,500+ ASSETS TO CLOUDFLARE PAGES (h03-themeapp-assets)
echo ===================================================================
wrangler.cmd pages deploy app\src\main\assets --    project-name h03-themeapp-assets --commit-dirty=true
echo.
echo ===================================================================
echo  DEPLOY COMPLETE! URL: https://h03-themeapp-assets.pages.dev
echo ===================================================================
pause
