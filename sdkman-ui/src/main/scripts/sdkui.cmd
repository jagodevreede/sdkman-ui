@echo off
if not exist "%HOME%\.sdkman" mkdir %HOME%\.sdkman
if not exist "%HOME%\.sdkman\tmp" mkdir %HOME%\.sdkman\tmp
if exist "%HOME%\.sdkman\tmp\exit-script.cmd" del %HOME%\.sdkman\tmp\exit-script.cmd

sdkman-ui.exe

:waitloop
IF EXIST "%HOME%\.sdkman\tmp\exit-script.cmd" GOTO waitloopend
timeout /t 1 /nobreak > nul
goto waitloop
:waitloopend
call %HOME%\.sdkman\tmp\exit-script.cmd
