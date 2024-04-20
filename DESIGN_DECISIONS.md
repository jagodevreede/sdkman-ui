# Design decisions

## Not using (de)compression in java
The JDK jars contain files with symlinks in them, this is not properly supported by for example commons-compress.
Also file permissions are not working, so files can't be executed.

Therefor the choice has been made to just use os tools like unzip and tar on osx and linux. And 7z on windows.