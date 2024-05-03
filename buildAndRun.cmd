@echo off
call mvnw install -Dmaven.test.skip=true
call mvnw javafx:jlink -f sdkman-ui
call sdkman-ui/target/sdkmanui/bin/launcher