@echo off
call mvnw install -Dmaven.test.skip=true
call mvnw javafx:run -f sdkman-ui