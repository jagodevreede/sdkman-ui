#!/usr/bin/env bash
mkdir -p ~/.sdkman/tmp
rm -f ~/.sdkman/tmp/exit-script.cmd

~/.sdkman/ui/sdkman-ui 2> /dev/null

if [ -f ~/.sdkman/tmp/exit-script.cmd ]; then
  source ~/.sdkman/tmp/exit-script.cmd
fi
