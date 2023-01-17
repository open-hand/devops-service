#!/bin/bash
set -e

RUNTIME_USER=$1
if [ -z $RUNTIME_USER ]; then
    RUNTIME_USER=$USER
fi

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
  sudo chown ${RUNTIME_USER}:${RUNTIME_USER} ${WORK_DIR}
  echo "Working directory ${WORK_DIR} created successfully"
fi

cd "$WORK_DIR" || exit

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
AGENT_LOG=${WORK_DIR}/${AGENT_NAME}.log
TAR_FILE=${WORK_DIR}/c7n-agent.tar.gz

EOF

sudo chmod 0777 ${WORK_DIR}/c7n-agent.env
sudo chown ${RUNTIME_USER}:${RUNTIME_USER} ${WORK_DIR}/c7n-agent.env

cat <<EOF | sudo tee ${WORK_DIR}/c7n-agent.sh
#!/bin/sh
operate=\$1
case \$operate in
start)
    /var/choerodon/c7n-agent --connect="${CONNECT}" --token="${TOKEN}" --hostId="${HOST_ID}" --version="${VERSION}" > ${WORK_DIR}/log/agent-cmd.log 2>&1
    ;;
stop)
    pidFile=/var/choerodon/c7n-agent.pid
    if [ -f \$pidFile ];then
      agentPid=\$(cat \$pidFile)
      kill -9 \$agentPid
      rm -rf \$pidFile
    fi
    ;;
esac

EOF

sudo chmod 0777 ${WORK_DIR}/c7n-agent.sh
sudo chown ${RUNTIME_USER}:${RUNTIME_USER} ${WORK_DIR}/c7n-agent.sh

# 4. 下载执行程序
echo "Downloading c7n-agent"
curl -Lo ${TAR_FILE} "{{ BINARY }}"

sudo chmod 0777 ${TAR_FILE}
sudo chown ${RUNTIME_USER}:${RUNTIME_USER} ${TAR_FILE}

rm -rf /var/choerodon/c7n-agent

tar -zxvf ${TAR_FILE}
echo "c7n-agent downloaded successfully"

sudo chmod 0777 ${AGENT}
sudo chown ${RUNTIME_USER}:${RUNTIME_USER} ${AGENT}

# 5. 配置systemd

cat <<EOF | sudo tee /usr/lib/systemd/system/c7n-agent.service
[Unit]
Description=Choerodon Host Manager Agent daemon

[Service]
EnvironmentFile=/var/choerodon/c7n-agent.env
ExecStart=/var/choerodon/c7n-agent.sh start
ExecStop=/var/choerodon/c7n-agent.sh stop
WorkingDirectory=/var/choerodon
Type=simple
KillMode=process
Restart=on-failure
RestartSec=30s
User=${RUNTIME_USER}
Group=${RUNTIME_USER}

[Install]
WantedBy=multi-user.target

EOF

sudo systemctl daemon-reload

# 6. 启动程序
cd "${WORK_DIR}"

if [ -f "/var/choerodon/c7n-agent.pid" ]; then
  sudo systemctl stop c7n-agent
fi

sudo systemctl start c7n-agent
