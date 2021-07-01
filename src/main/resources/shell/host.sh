# 1. 将用户加入docker组中
sudo groupadd docker
sudo gpasswd -a ${USER} docker
sudo service docker restart
sudo newgrp - docker
# 2. 新建目录
if [ ! -x "$myPath"]; then
mkdir "$myPath"
fi
# 3. 下载执行程序
curl -o agent http://file.com/agent
# 4. 启动程序
./agent