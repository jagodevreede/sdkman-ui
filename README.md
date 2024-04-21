# sdkman-ui

This project aims to offer a Graphical User Interface for [SDKMAN](https://sdkman.io/). And a out of the box
implementation for windows.

> This project is still under heavy development and testing. And is not yet ready for public use!

## Sneek peak at UI (osx):

![Ui sneek peak](https://jagodevreede.github.io/sdkman-ui-images/Animatie.gif)

## Available platforms

Some platforms are not yet available as they are not yet setup and/or tested.

| Platform    | Available |  
|-------------|-----------|
| Windows x86 |           |
| Linux x86   |           |           
| Linux arm   |           |
| osx x86     |           |
| osx arm     |           |

## build steps:

- `sdk u java 22.1.0.1.r17-gln` or manually install from https://github.com/gluonhq/graal/releases
- `export GRAALVM_HOME=$JAVA_HOME`
- `./mvnw clean install -DskipTests`

Native:

`./mvnw gluonfx:build -f sdkman-ui`

Jlink:

-`./mvnw javafx:jlink -f sdkman-ui`

run with: `./sdkman-ui/target/sdkmanui/bin/launcher`

## Design decisions

See [DESIGN_DECISIONS.md](DESIGN_DECISIONS.md)