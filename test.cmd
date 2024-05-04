@ECHO OFF
rem Run next command elevated to Admin.
set __COMPAT_LAYER=RunAsAdmin
echo "as admin"
rem Disable elevation
set __COMPAT_LAYER=
rem continue non elevated
echo "as user


java.nio.file.FileSystemException: C:\Users\win-test\.sdkman\candidates\java\current: A required privilege is not held by the client