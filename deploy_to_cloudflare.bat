@echo off
set PATH=C:\Program Files\nodejs;%APPDATA%\npm;%PATH%
echo ===================================================================
echo  DEPLOYING ALL ASSETS TO CLOUDFLARE PAGES (h03-themeapp-assets)
echo  Source Folder: cdn_assets_source
echo ===================================================================
wrangler.cmd pages deploy cdn_assets_source --project-name=h03-themeapp-assets --branch=main --commit-dirty=true
echo.
echo ===================================================================
echo  DEPLOY COMPLETE! URL: https://h03-themeapp-assets.pages.dev
echo ===================================================================
pause
