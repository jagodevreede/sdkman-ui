# Design decisions

## Not using (de)compression in java
The JDK jars contain files with symlinks in them, this is not properly supported by for example commons-compress.
Also file permissions are not working, so files can't be executed.

Therefor the choice has been made to just use os tools like unzip and tar on osx and linux. And 7z on windows.

## native-image folder
The files generated by the agent in native-image are very os dependent, so we need a set for every os, the best solution 
would be to set the `agentDir` property in the gluon plugin, however does not work. So we copy the files manually. First 
in the `validate` phase and then we need to put it back manually.
