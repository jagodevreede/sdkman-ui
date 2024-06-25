# sdkman-ui

This project aims to offer a Graphical User Interface for [SDKMAN](https://sdkman.io/). And a out of
the box implementation for windows.

> This project is still under heavy development and testing. And is not yet ready for public use!

## Sneek peak at UI (osx):

![Ui sneek peak](https://jagodevreede.github.io/sdkman-ui-images/Animatie.gif)

## Requirements

tar, unzip and zip should be present on the system. You can install them with your favourite package
manager on *inx systems.

### Windows:

- tar: no need for tar as it is not used on windows
- zip: [https://gnuwin32.sourceforge.net/packages/zip.htm](https://gnuwin32.sourceforge.net/packages/zip.htm)
or [direct download](http://downloads.sourceforge.net/gnuwin32/zip-3.0-bin.zip)
- unzip: [https://gnuwin32.sourceforge.net/packages/unzip.htm](https://gnuwin32.sourceforge.net/packages/unzip.htm)
or [direct download](https://gnuwin32.sourceforge.net/downlinks/unzip-bin-zip.php)

#### Symlinks

Most versions of Windows do not allow the creation on symlinks by default, SDKMAN UI will work
around that by using copy, however this is a lot slower. You can enable development mode in windows
see [https://learn.microsoft.com/en-us/windows/apps/get-started/enable-your-device-for-development](https://learn.microsoft.com/en-us/windows/apps/get-started/enable-your-device-for-development)
for more information.

## Available platforms

Some platforms are not yet available as they are not yet setup and/or tested.

| Platform    | Available |  
|-------------|-----------|
| Windows x86 |           |
| Linux x86   |           |           
| Linux arm   |           |
| osx x86     |           |
| osx arm     |           |

## Install instructions

### Windows

Download the latest version and extract the zip, then run the `install.cmd`. Then it should be
available from the
commandline as `sdkui`

## build steps:

- `sdk u java 22.1.0.1.r17-gln` or manually install from https://github.com/gluonhq/graal/releases
- `export GRAALVM_HOME=$JAVA_HOME`
- `./mvnw clean install -DskipTests`

Native:

To update native reflections `gluonfx:runagent`
`./mvnw gluonfx:build -f sdkman-ui`

Jlink:

-`./mvnw javafx:jlink -f sdkman-ui`

run with: `./sdkman-ui/target/sdkmanui/bin/launcher`

## Run as developer:

Run the following class:
`io.github.jagodevreede.sdkmanui.Main`

## Design decisions

See [DESIGN_DECISIONS.md](DESIGN_DECISIONS.md)