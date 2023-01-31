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

# 3. 保存环境变量
cat <<EOF | tee ${WORK_DIR}/c7n-agent.env
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

chmod 0777 ${WORK_DIR}/c7n-agent.env

cat <<EOF | tee ${WORK_DIR}/c7n-agent.sh
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

chmod 0777 ${WORK_DIR}/c7n-agent.sh

cd "$WORK_DIR" || exit

# 4. 下载执行程序
echo "Downloading c7n-agent"
curl -o ${TAR_FILE} "{{ BINARY }}"

rm -rf /var/choerodon/c7n-agent

tar -zxvf ${TAR_FILE}
echo "c7n-agent downloaded successfully"
