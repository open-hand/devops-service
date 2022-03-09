#!/bin/bash
set -e

VAR=/var
WORK_DIR=${VAR}/choerodon
TOKEN={{ TOKEN }}
CONNECT={{ CONNECT }}
HOST_ID={{ HOST_ID }}
VERSION={{ VERSION }}
AGENT_NAME=c7n-agent
AGENT=${WORK_DIR}/${AGENT_NAME}
TAR_FILE=${WORK_DIR}/c7n-agent.tar.gz

# 1. 校验当前用户有var目录
if [ -z "${VAR}" ]; then
  sudo mkdir /var
fi

# 2. 创建choerodon目录
if [ ! -d "${WORK_DIR}" ]; then
  echo "Creating ${WORK_DIR} directory"
  sudo mkdir $WORK_DIR
  sudo chmod 0777 $WORK_DIR
  echo "Working directory ${WORK_DIR} created successfully"
fi

# 3. 保存环境变量
cat <<EOF | sudo tee ${WORK_DIR}/c7n-agent.env
VAR=/var
WORK_DIR=${VAR}/choerodon
TOKEN={{ TOKEN }}
CONNECT={{ CONNECT }}
HOST_ID={{ HOST_ID }}
VERSION={{ VERSION }}
AGENT_NAME=c7n-agent
AGENT=${WORK_DIR}/${AGENT_NAME}
TAR_FILE=${WORK_DIR}/c7n-agent.tar.gz

EOF
sudo chmod 0777 ${WORK_DIR}/agent.env

cd "$WORK_DIR" || exit

# 4. 下载执行程序
echo "Downloading c7n-agent"
curl -o ${TAR_FILE} "{{ BINARY }}"

if [ -f $AGENT_NAME ];then
rm -rf /var/choerodon/c7n-agent
fi
tar -zxvf ${TAR_FILE}
echo "c7n-agent downloaded successfully"