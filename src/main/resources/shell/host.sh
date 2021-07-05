#!/bin/bash
#set -e

TOKEN={{ TOKEN }}
CONNECT={{ CONNECT }}
HOST_ID={{ HOST_ID }}

# 1. 校验当前用户有HOME目录
if [ "$HOME" ];then
    echo "请创建用户目录，并设置HOME变量"
    exit 1
fi

# 2. 将用户加入docker组中
sudo gpasswd -a "${USER}" docker
sudo systemctl restart docker
newgrp - docker

# 3. 创建choerodon目录
mkdir "$HOME"/choerodon
chmod 0777 "$HOME"/choerodon
WORK_DIR=$HOME/choerodon

cd "$WORK_DIR" || exit

# 4. 下载执行程序
curl -o "$WORK_DIR"/c7n-agent {{ BINARY }}

# 5. 启动程序
nohup ./c7n-agent --connect="${CONNECT}" --token="${TOKEN}" --hostId="${HOST_ID}" > agent.log 2>&1 &

# 6. 保存agent进程号
echo $! > c7n-agent.pid