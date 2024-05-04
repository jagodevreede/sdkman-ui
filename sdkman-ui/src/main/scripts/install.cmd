@echo off
setlocal
set SDKMAN_PATH=%homedrive%%homepath%\.sdkman
set INSTALL_PATH=%SDKMAN_PATH%\ui

for /f "usebackq tokens=3" %%i in (`reg.exe query HKCU\Environment /v Path`) do set oldpath=%%i

REM Test if skdman ui is already on the path
If /I Not "%oldpath%"=="!oldpath:%INSTALL_PATH%=!" (set SDKMAN_UI_ON_PATH=YES) Else set SDKMAN_UI_ON_PATH=NO

echo SDKMAN UI will be installed to %INSTALL_PATH%
:PROMPT
SET /P AREYOUSURE=Do you want to continue? (Y/[N])?
IF /I "%AREYOUSURE%" NEQ "Y" GOTO END

if not exist "%SDKMAN_PATH%" mkdir %SDKMAN_PATH%
if not exist "%INSTALL_PATH%" mkdir %INSTALL_PATH%

IF /I "%SDKMAN_UI_ON_PATH%" NEQ "NO" GOTO PATH_ADDED
reg add HKCU\Environment /v Path /t REG_EXPAND_SZ /d %INSTALL_PATH%;%oldpath%; /f
echo Added SKDMAN UI to path, open a new terminal window to make sure it works
:PATH_ADDED

copy sdkman-ui.exe %INSTALL_PATH%\sdkman-ui.exe
copy sdkui.cmd %INSTALL_PATH%\sdkui.cmd

:END
endlocal

