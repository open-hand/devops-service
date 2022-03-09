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

cat <<EOF | sudo tee ${WORK_DIR}/c7n-agent.sh
#!/bin/sh
operate=\$1
case \$operate in
start)
    /var/choerodon/c7n-agent --connect="${CONNECT}" --token="${TOKEN}" --hostId="${HOST_ID}" --version="${VERSION}"
    ;;
stop)
    agentPid=\$(cat /var/choerodon/c7n-agent.pid)
    if [ ! -z \$agentPid ];then
    kill -9 \$agentPid
    rm -rf /var/choerodon/c7n-agent.pid
    fi
    ;;
esac

EOF

sudo chmod 0777 ${WORK_DIR}/c7n-agent.sh

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

sudo chmod 0777 ${WORK_DIR}/agent.env


# 4. 下载执行程序
echo "Downloading c7n-agent"
curl -o ${TAR_FILE} "{{ BINARY }}"

if [ -f $AGENT_NAME ];then
rm -rf /var/choerodon/c7n-agent
fi
tar -zxvf ${TAR_FILE}
echo "c7n-agent downloaded successfully"

# 5. 配置systemd

cat <<EOF | sudo tee /usr/lib/systemd/system/c7n-agent.service
[Unit]
Description=Choerodon Host Manager Agent daemon

[Service]
EnvironmentFile=/var/choerodon/c7n-agent.env
ExecStart=/var/choerodon/c7n-agent.sh start
ExecStop=/var/choerodon/c7n-agent.sh stop
Type=simple
KillMode=process
Restart=on-failure
RestartSec=30s
User=$USER
Group=$USER

[Install]
WantedBy=multi-user.target

EOF

sudo systemctl daemon-reload

# 6. 启动程序
cd "${WORK_DIR}"
chmod +x "${AGENT}"

if [ -f "/var/choerodon/c7n-agent.pid" ];then
  agentPid=$(cat /var/choerodon/c7n-agent.pid)
  if [ ! -z $agentPid ];then
    sudo systemctl stop c7n-agent
  fi
fi

sudo systemctl start c7n-agent