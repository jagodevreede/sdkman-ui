@echo off
if not exist "%homedrive%%homepath%\.sdkman" mkdir %homedrive%%homepath%\.sdkman
if not exist "%homedrive%%homepath%\.sdkman\tmp" mkdir %homedrive%%homepath%\.sdkman\tmp
if exist "%homedrive%%homepath%\.sdkman\tmp\exit-script.cmd" del %homedrive%%homepath%\.sdkman\tmp\exit-script.cmd

sdkman-ui.exe

:waitloop
IF EXIST "%homedrive%%homepath%\.sdkman\tmp\exit-script.cmd" GOTO waitloopend
timeout /t 1 /nobreak > nul
goto waitloop
:waitloopend
call %homedrive%%homepath%\.sdkman\tmp\exit-script.cmd
