# sdkman-ui

# build:

`sdk u java 22.1.0.1.r17-gln`
`export GRAALVM_HOME=$JAVA_HOME`
`./mvnw clean install -DskipTests`
`./mvnw gluonfx:build -f sdkman-ui`