@echo off
setlocal enabledelayedexpansion

echo Checking Go installation...
where go >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Error: Go is not installed or not in PATH. Please install Go from https://go.dev/dl/
    exit /b 1
)

echo Checking gomobile...
where gomobile >nul 2>nul
if %ERRORLEVEL% neq 0 (
    if exist "%USERPROFILE%\go\bin\gomobile.exe" (
        set "PATH=%USERPROFILE%\go\bin;%PATH%"
    ) else (
        echo Installing gomobile...
        go install golang.org/x/mobile/cmd/gomobile@latest
        set "PATH=%USERPROFILE%\go\bin;%PATH%"
        gomobile init
    )
)

set "ROOT_DIR=%~dp0.."
set "DATAPATH_DIR=%ROOT_DIR%\datapath"
set "OUTPUT_DIR=%ROOT_DIR%\app\build\gomobile"
set "LIBS_DIR=%ROOT_DIR%\app\libs"

if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"
if not exist "%LIBS_DIR%" mkdir "%LIBS_DIR%"

echo Building datapath.aar for android/arm64...
cd /d "%DATAPATH_DIR%"
gomobile bind -target=android/arm64 -androidapi 24 -o "%OUTPUT_DIR%\datapath.aar" .

copy /Y "%OUTPUT_DIR%\datapath.aar" "%LIBS_DIR%\datapath.aar" >nul

echo Successfully built datapath.aar at:
echo   - %OUTPUT_DIR%\datapath.aar
echo   - %LIBS_DIR%\datapath.aar
