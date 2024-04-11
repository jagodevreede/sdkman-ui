# sdkman-ui

# build:

`sdk u java 22.1.0.1.r17-gln` or manually install from https://github.com/gluonhq/graal/releases
`export GRAALVM_HOME=$JAVA_HOME`
`./mvnw clean install -DskipTests`
Native:
`./mvnw gluonfx:build -f sdkman-ui`
Jlink:
`./mvnw javafx:jlink -f sdkman-ui`
run: `./sdkman-ui/target/sdkmanui/bin/launcher` 