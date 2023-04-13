#!/bin/bash
set -e
WORK_DIR="{{ WORK_DIR }}"
APP_FILE_NAME="{{ APP_FILE_NAME }}"
APP_FILE="{{ APP_FILE }}"
cd ${WORK_DIR}
{{ COMMAND }}
