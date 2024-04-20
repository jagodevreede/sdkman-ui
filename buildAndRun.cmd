@echo off
call mvnw install -DskipTests
call mvnw javafx:jlink -f sdkman-ui
call sdkman-ui/target/sdkmanui/bin/launcher