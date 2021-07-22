set -e
WORKING_PATH={{ WORKING_PATH }}
#创建工作目录
if [ ! -d $WORKING_PATH ]; then
  mkdir -p ${WORKING_PATH}/temp-jar
  mkdir -p ${WORKING_PATH}/temp-log
fi

# 下载jar包
curl -o {{ JAR_NAME }} -u {{ USER_ID }}:{{ PASSWORD }} {{ DOWNLOAD_URL }}

{{ KILL_JAR_PROCESS }}
# 部署命令
nohup {{ JAVA_JAR_EXEC }} &

echo $!
