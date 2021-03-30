#!/usr/bin/env sh
{{ install-command }}
exitCode=$?
echo $exitCode > {{exit-code-path}}
if [ $exitCode != 0 ]; then
  cat /tmp/install.log >&2
  exit 1
fi
