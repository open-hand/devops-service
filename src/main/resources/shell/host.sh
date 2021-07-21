#!/bin/bash
set -e

TOKEN={{ TOKEN }}
CONNECT={{ CONNECT }}
HOST_ID={{ HOST_ID }}
VERSION={{ VERSION }}

# 1. 校验当前用户有HOME目录
if [ -z "$HOME" ]; then
  echo "Please create a user home directory and set the HOME variable"
  exit 1
fi

# 2. 创建choerodon目录
WORK_DIR=$HOME/choerodon
if [ ! -d "${WORK_DIR}" ]; then
  echo "Creating ${WORK_DIR} directory"
  mkdir "$HOME"/choerodon
  chmod 0777 "$HOME"/choerodon
  echo "Working directory ${WORK_DIR} created successfully"
fi

cd "$WORK_DIR" || exit

AGENT_NAME="c7n-agent"

AGENT=${WORK_DIR}/${AGENT_NAME}-${VERSION}
AGENT_LOG=$WORK_DIR/${AGENT_NAME}.log

# 3. 下载执行程序
echo "Downloading c7n-agent"
curl -o "${AGENT}" "{{ BINARY }}"
echo "c7n-agent downloaded successfully"

# 4. 启动程序
cd "${WORK_DIR}"
chmod +x "${AGENT}"
nohup "${AGENT}" --connect="${CONNECT}" --token="${TOKEN}" --hostId="${HOST_ID}" --version="${VERSION}" >"${AGENT_LOG}" 2>&1 &

# 5. 保存agent进程号
echo $! >c7n-agent.pid
echo "c7n-agent started successfully"