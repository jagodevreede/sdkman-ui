@echo off
setlocal
cd "%homedrive%%homepath%\.sdkman\ui"

:check_and_replace

if exist "sdkman-ui-update.exe" (
    del /f /q "sdkman-ui.exe"

    if not exist "sdkman-ui.exe" (
        ren "sdkman-ui-update.exe" "sdkman-ui.exe"
        
        if exist "sdkman-ui.exe" (
            goto start_program
        ) else (
            timeout /t 1 /nobreak > nul
            goto check_and_replace
        )
    ) else (
        timeout /t 1 /nobreak > nul
        goto check_and_replace
    )
)

:start_program
sdkman-ui.exe --update-complete
exit /b
