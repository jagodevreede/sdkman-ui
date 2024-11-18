#!/usr/bin/env bash

cd "$HOME/.sdkman/ui"

check_and_replace() {
  if [[ -f "sdkman-ui-update" ]]; then
    rm -f "sdkman-ui"
    
    if [[ ! -f "sdkman-ui" ]]; then
      mv "sdkman-ui-update" "sdkman-ui"
      
      if [[ -f "sdkman-ui" ]]; then
        start_program
        return
      else
        sleep 1
        check_and_replace
      fi
    else
      sleep 1
      check_and_replace
    fi
  fi
}

start_program() {
  ./sdkman-ui --update-complete
  exit 0
}

check_and_replace
