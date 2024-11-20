# sdkman-ui

This project aims to offer a (cross-platform) Graphical User Interface for [SDKMAN](https://sdkman.io/).

> [!NOTE]  
> Primary focus for now is to create a UI for windows. Osx for intel is available arm will be added later. Linux x86
> will be next.

## Screenshots

<p align="center">
    <img height="150" src="https://jagodevreede.github.io/sdkman-ui-images/windows version 0.2.0.png" />
    <img height="150" src="https://jagodevreede.github.io/sdkman-ui-images/osx version 0.2.0.png" />
</p>

## Available platforms

Some platforms are not yet available as they are not yet setup and/or tested.

| Platform    | Available                                                                                                       | Supported envirmoments |
|-------------|-----------------------------------------------------------------------------------------------------------------|------------------------|
| Windows x86 | [v0.2.0](https://github.com/jagodevreede/sdkman-ui/releases/download/v0.2.0/sdkman-ui-windows_x86_64-0.2.0.zip) | cmd                    |
| Linux x86   |                                                                                                                 | bash, zsh              |      
| Linux arm   |                                                                                                                 | bash, zsh              |
| osx x86     | [v0.2.0](https://github.com/jagodevreede/sdkman-ui/releases/download/v0.2.0/sdkman-ui-osx_x86_64-0.2.0.zip)     | bash, zsh              |
| osx arm     |                                                                                                                 | bash, zsh              |

Early access builds can be found
here: [releases/tag/early-access](https://github.com/jagodevreede/sdkman-ui/releases/tag/early-access)

## Requirements

tar, unzip and zip should be present on the system. You can install them with your favourite package manager on *inx
systems.

### Windows:

Zip and unzip are bundled with the application, if they are on the path then skdman-ui will use those if not then it
will use the bundled versions:

- tar: no need for tar as it is not used on windows
- zip: [https://gnuwin32.sourceforge.net/packages/zip.htm](https://gnuwin32.sourceforge.net/packages/zip.htm)
  or [direct download](http://downloads.sourceforge.net/gnuwin32/zip-3.0-bin.zip)
- unzip: [https://gnuwin32.sourceforge.net/packages/unzip.htm](https://gnuwin32.sourceforge.net/packages/unzip.htm)
  or [direct download](https://gnuwin32.sourceforge.net/downlinks/unzip-bin-zip.php)

## Install instructions

### Windows

Download the latest version and extract the zip, then run the `sdkman-ui.exe`. And follow on screen instructions. Then
it should be available from the commandline as `sdkui`.

Also see the following [video](https://www.youtube.com/watch?v=oyYtHrihThk)

#### Symlinks

Most versions of Windows do not allow the creation on symlinks by default, SDKman UI will work
around that by using copy, however this is a lot slower. You can enable development mode in windows
see [https://learn.microsoft.com/en-us/windows/apps/get-started/enable-your-device-for-development](https://learn.microsoft.com/en-us/windows/apps/get-started/enable-your-device-for-development)
for more information.

### OSX

First install [SDKMAN](https://sdkman.io/) if you have not already done so. as SDKman UI relies on SDKMAN.

Download the latest version with the correct cpu type (x86_64 or aarch64) and extract the zip, then run the `sdkman-ui`
osx will complain about unable to verify developer identity, click ok and then go to privacy & security settings under
system settings and click "Open Anyway". Then follow on screen instructions.

## build steps:

- `sdk u java 22.1.0.1.r17-gln` or manually install from https://github.com/gluonhq/graal/releases
- `export GRAALVM_HOME=$JAVA_HOME`
- `./mvnw clean install -DskipTests`

### Native:

Prerequisites: See https://www.graalvm.org/latest/reference-manual/native-image/#prerequisites

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