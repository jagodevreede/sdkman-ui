# sdkman-ui

This project aims to offer a (cross-platform) Graphical User Interface for [SDKMAN](https://sdkman.io/), with the
primary focus on windows. However, other OS ports are working as well.

## Screenshots

<p align="center">
    <img height="150" src="https://jagodevreede.github.io/sdkman-ui-images/windows version 0.2.0.png" />
    <img height="150" src="https://jagodevreede.github.io/sdkman-ui-images/osx version 0.2.0.png" />
</p>

## Available platforms

Some platforms are not yet available as they are not yet setup and/or tested.

| Platform    | Available                                                                                                       | Supported envirmoments |
|-------------|-----------------------------------------------------------------------------------------------------------------|------------------------|
| Windows x86 | [v0.3.1](https://github.com/jagodevreede/sdkman-ui/releases/download/v0.3.1/sdkman-ui-windows_x86_64-0.3.1.zip) | cmd                    |
| Linux x86   | [v0.3.1](https://github.com/jagodevreede/sdkman-ui/releases/download/v0.3.1/sdkman-ui-linux_x86_64-0.3.1.zip)   | bash, zsh              |      
| Linux arm   |                                                                                                                 | bash, zsh              |
| osx x86     | [v0.3.1](https://github.com/jagodevreede/sdkman-ui/releases/download/v0.3.1/sdkman-ui-osx_x86_64-0.3.1.zip)     | bash, zsh              |
| osx arm     | [v0.3.1](https://github.com/jagodevreede/sdkman-ui/releases/download/v0.3.1/sdkman-ui-osx_aarch64-0.3.1.zip) *  | bash, zsh              |

* Osx aarch64 seems to have some issues but might work on your machine, please raise any issues you encounter.

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

#### Windows troubleshooting

If you get the error that the `vcruntime140.dll` file is missing, then the download and install the appropriate version for your os of the Visual C++ redistributable package:

- x86: [vc_redist.x86.exe](https://aka.ms/vs/16/release/vc_redist.x86.exe)
- x64: [vc_redist.x64.exe](https://aka.ms/vs/16/release/vc_redist.x64.exe)

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

### Linux

First install [SDKMAN](https://sdkman.io/) if you have not already done so. as SDKman UI relies on SDKMAN.

Download the latest version with the correct cpu type (x86_64 or aarch64) and extract the zip, then run the `sdkman-ui`
from the terminal. If you run it without a terminal it will freeze up.

#### Arch linux notes

If the application loads but without text, then you might need to install the following package

  sudo pacman -S ttf-dejavu


## build steps:

- `sdk u java 22.1.0.1.r17-gln` or manually install from https://github.com/gluonhq/graal/releases
- `export GRAALVM_HOME=$JAVA_HOME`
- `./mvnw clean install -DskipTests`

### Native:

Prerequisites: See https://www.graalvm.org/latest/reference-manual/native-image/#prerequisites

- To update native reflections `./mvnw gluonfx:runagent -f sdkman-ui`
- then run a build: `./mvnw gluonfx:build -f sdkman-ui`

If you encounter a `linker command failed` then you need to do the following instead of the `gluonfx:build`
- `./mvnw gluonfx:compile -f sdkman-ui`
- `find . -name libjnidispatch.a -type f -delete`
- `./mvnw gluonfx:link -f sdkman-ui`

Jlink:

-`./mvnw javafx:jlink -f sdkman-ui`

run with: `./sdkman-ui/target/sdkmanui/bin/launcher`

## Run as developer:

Run the following class:
`io.github.jagodevreede.sdkmanui.Main`

## Design decisions

See [DESIGN_DECISIONS.md](DESIGN_DECISIONS.md)