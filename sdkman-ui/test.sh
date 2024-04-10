#!/bin/bash

function sdkui() {
  mkdir -p source ~/.sdkman/tmp
  rm -f ~/.sdkman/tmp/exit-script.sh
  touch ~/.sdkman/tmp/exit-script.sh

  /Users/jagodevreede/git/sdkman-ui/sdkman-ui/target/sdkmanui/bin/launcher

  source ~/.sdkman/tmp/exit-script.sh
}