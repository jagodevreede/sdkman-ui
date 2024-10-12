@echo off
setlocal
CALL :GETPARENT PARENT
IF /I "%PARENT%" == "powershell" GOTO :ISPOWERSHELL
IF /I "%PARENT%" == "pwsh" GOTO :ISPOWERSHELL
endlocal

echo Not running from Powershell 
SET EnvVar=MyValue

GOTO :EOF

:GETPARENT
SET "PSCMD=$ppid=$pid;while($i++ -lt 3 -and ($ppid=(Get-CimInstance Win32_Process -Filter ('ProcessID='+$ppid)).ParentProcessId)) {}; (Get-Process -EA Ignore -ID $ppid).Name"

for /f "tokens=*" %%i in ('powershell -noprofile -command "%PSCMD%"') do SET %1=%%i

GOTO :EOF

:ISPOWERSHELL
SET EnvVar2=Other
$env:EnvVar='MyValue'
Set-Item -Path Env:EnvVar - Value 'MyValue'
echo. >&2
echo ERROR: This batch file must not be run from a PowerShell prompt >&2
echo. >&2
exit /b 1