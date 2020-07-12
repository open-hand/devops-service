# 获取的组织编码
export ORG_CODE={{ ORG_CODE }}
# 获取的项目编码
export PRO_CODE={{ PRO_CODE }}
# 获取的应用名称
export PROJECT_NAME={{ PROJECT_NAME }}
# 应用harbor仓库地址
export DOCKER_REGISTRY={{ DOCKER_REGISTRY }}
# 应用harbor仓库用户名
export DOCKER_USERNAME={{ DOCKER_USERNAME }}
# 兼容以往harbor仓库用户名变量
export DOCKER_USER=$DOCKER_USERNAME
# 应用harbor用户密码
export DOCKER_PASSWORD={{ DOCKER_PASSWORD }}
# 兼容以往harbor仓库用户密码变量
export DOCKER_PWD=$DOCKER_PASSWORD
# 获取的组织编码-项目编码(harbor Project地址)
export GROUP_NAME={{ GROUP_NAME }}
# SONARQUBE的地址
export SONAR_URL={{ SONAR_URL }}
# SONARQUBE的token
export SONAR_LOGIN={{ SONAR_LOGIN }}
# HARBOR配置Id
export HARBOR_CONFIG_ID={{ HARBOR_CONFIG_ID }}
# 设置docekr认证配置文件目录
export DOCKER_CONFIG=$PWD/.choerodon/.docker

# 创建docekr认证配置文件目录
mkdir -p $DOCKER_CONFIG
# 设成docekr认证配置文件
echo "{\"auths\":{\"$DOCKER_REGISTRY\":{\"auth\":\"$(echo -n $DOCKER_USERNAME:$DOCKER_PASSWORD | base64)\"}}}" | tr -d '\n' > $DOCKER_CONFIG/config.json

# 获取commit时间
C7N_COMMIT_TIMESTAMP=$(git log -1 --pretty=format:"%ci" | awk '{print $1$2}' | sed 's/[-:]//g')
C7N_COMMIT_YEAR=${C7N_COMMIT_TIMESTAMP:0:4}
C7N_COMMIT_MONTH=$(echo ${C7N_COMMIT_TIMESTAMP:4:2} | sed s'/^0//')
C7N_COMMIT_DAY=$(echo ${C7N_COMMIT_TIMESTAMP:6:2} | sed s'/^0//')
C7N_COMMIT_HOURS=${C7N_COMMIT_TIMESTAMP:8:2}
C7N_COMMIT_MINUTES=${C7N_COMMIT_TIMESTAMP:10:2}
C7N_COMMIT_SECONDS=${C7N_COMMIT_TIMESTAMP:12:2}
export C7N_COMMIT_TIME=$C7N_COMMIT_YEAR.$C7N_COMMIT_MONTH.$C7N_COMMIT_DAY-$C7N_COMMIT_HOURS$C7N_COMMIT_MINUTES$C7N_COMMIT_SECONDS

# 8位sha值
export C7N_COMMIT_SHA=$(git log -1 --pretty=format:"%H" | awk '{print substr($1,1,8)}')

# 分支名
if [ $CIRCLECI ]; then
  export C7N_BRANCH=$(echo $CIRCLE_BRANCH | tr '[A-Z]' '[a-z]' | tr '[:punct:]' '-')
elif [ $GITLAB_CI ]; then
  export C7N_BRANCH=$CI_COMMIT_REF_SLUG
fi

# 默认Version
if [ $CI_COMMIT_TAG ]; then
  export C7N_VERSION=$CI_COMMIT_TAG
elif [ $CIRCLE_TAG ]; then
  export C7N_VERSION=$CIRCLE_TAG
else
  export C7N_VERSION=$C7N_COMMIT_TIME-$C7N_BRANCH
fi

export CI_COMMIT_TAG=$C7N_VERSION

# 参数为要合并的远程分支名,默认develop
# e.g. git_merge develop
function git_merge() {
  git config user.name ${GITLAB_USER_NAME}
  git config user.email ${GITLAB_USER_EMAIL}
  git checkout origin/${1:-"develop"}
  git merge ${CI_COMMIT_SHA} --no-commit --no-ff
}

#################################### 前端相关函数 ####################################
function node_module() {
  mkdir -p /cache/${CI_PROJECT_NAMESPACE}-${CI_PROJECT_NAME}-${CI_COMMIT_SHA}
  python ./boot/structure/configAuto.py ${1}
  cp -r config.yml /cache/${CI_PROJECT_NAMESPACE}-${CI_PROJECT_NAME}-${CI_COMMIT_SHA}
  cd boot && npm install && cd ../${1} && npm install && cd ..
}

# 开发使用devbuild，构建镜像使用build
function node_build() {
  cd boot
  ./node_modules/.bin/gulp start
  cnpm run ${1:-"build"}
  find dist -name '*.js' | xargs sed -i "s/localhost:version/$CI_COMMIT_TAG/g"
}

function cache_dist() {
  cp -r dist /cache/${CI_PROJECT_NAMESPACE}-${CI_PROJECT_NAME}-${CI_COMMIT_SHA}/dist
}

#################################### 微服务相关函数 ####################################
# 更新maven项目本版本号
# $1 填入true表示用项目根目录的settings.xml文件，其他任何值都不使用本地settings.xml
function update_pom_version() {
  if [ "$1" == "true" -a -f settings.xml ];then
      echo "Update pom version: using custom settings.xml..."
      mvn versions:set -DnewVersion=${CI_COMMIT_TAG} -s settings.xml ||
              find . -name pom.xml | xargs xml ed -L \
                -N x=http://maven.apache.org/POM/4.0.0 \
                -u '/x:project/x:version' -v "${CI_COMMIT_TAG}"
      mvn versions:commit -s settings.xml
  else
      echo "Update pom version: using default settings.xml..."
      mvn versions:set -DnewVersion=${CI_COMMIT_TAG} ||
        find . -name pom.xml | xargs xml ed -L \
          -N x=http://maven.apache.org/POM/4.0.0 \
          -u '/x:project/x:version' -v "${CI_COMMIT_TAG}"
      mvn versions:commit
  fi
}

function database_test() {
  while ! mysqlcheck --host=127.0.0.1 --user=root --password=${MYSQL_ROOT_PASSWORD} mysql; do sleep 1; done
  echo "CREATE DATABASE hap_cloud_test DEFAULT CHARACTER SET utf8;" |
    mysql --user=root --password=${MYSQL_ROOT_PASSWORD} --host=127.0.0.1
  java -Dspring.datasource.url="jdbc:mysql://127.0.0.1/hap_cloud_test?useUnicode=true&characterEncoding=utf-8&useSSL=false" \
    -Dspring.datasource.username=root \
    -Dspring.datasource.password=${MYSQL_ROOT_PASSWORD} \
    -Ddata.dir=src/main/resources \
    -jar /var/hapcloud/hap-liquibase-tool.jar
}

function cache_jar() {
  mkdir -p ${HOME}/.m2/${CI_PROJECT_NAMESPACE}-${CI_PROJECT_NAME}-${CI_COMMIT_SHA}
  cp target/app.jar ${HOME}/.m2/${CI_PROJECT_NAMESPACE}-${CI_PROJECT_NAME}-${CI_COMMIT_SHA}/app.jar
}

#################################### 构建镜像 ####################################
function docker_build() {
  cp ${HOME}/.m2/${CI_PROJECT_NAMESPACE}-${CI_PROJECT_NAME}-${CI_COMMIT_SHA}/app.jar ${1:-"src/main/docker"}/app.jar || true
  cp -r /cache/${CI_PROJECT_NAMESPACE}-${CI_PROJECT_NAME}-${CI_COMMIT_SHA}/* ${1:-"."} || true
  docker build -t ${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${CI_COMMIT_TAG} ${1:-"."} || true
  docker build -t ${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${CI_COMMIT_TAG} ${1:-"src/main/docker"} || true
  docker push ${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${CI_COMMIT_TAG}
}

#################################### 清理缓存 ####################################
function clean_cache() {
  rm -rf ${HOME}/.m2/${CI_PROJECT_NAMESPACE}-${CI_PROJECT_NAME}-${CI_COMMIT_SHA}
  rm -rf /cache/${CI_PROJECT_NAMESPACE}-${CI_PROJECT_NAME}-${CI_COMMIT_SHA}
}

################################ 上传生成的chart包到猪齿鱼平台的devops-service ##################################
# 此项为上传构建并上传chart包到Choerodon中，只有通过此函数Choerodon才会有相应版本记录。
function chart_build() {
  #判断chart主目录名是否与应用编码保持一致
  CHART_DIRECTORY_PATH=$(find . -maxdepth 2 -name ${PROJECT_NAME})
  if [ ! -n "${CHART_DIRECTORY_PATH}" ]; then
    echo "The chart's home directory should be consistent with the application code!"
    exit 1
  fi
  # 查找Chart.yaml文件
  CHART_PATH=$(find . -maxdepth 3 -name Chart.yaml)
  # 重置values.yaml文件中image.repository属性
  sed -i "s,repository:.*$,repository: ${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME},g" ${CHART_PATH%/*}/values.yaml
  # 构建chart包，重写version与app-version为当前版本
  helm package ${CHART_PATH%/*} --version ${CI_COMMIT_TAG} --app-version ${CI_COMMIT_TAG}
  TEMP=${CHART_PATH%/*}
  FILE_NAME=${TEMP##*/}
  # 通过Choerodon API上传chart包到devops-service
  result_upload_to_devops=$(curl -X POST \
    -H 'Expect:' \
    -F "token=${Token}" \
    -F "harbor_config_id=${HARBOR_CONFIG_ID}" \
    -F "version=${CI_COMMIT_TAG}" \
    -F "file=@${FILE_NAME}-${CI_COMMIT_TAG}.tgz" \
    -F "commit=${CI_COMMIT_SHA}" \
    -F "ref=${CI_COMMIT_REF_NAME}" \
    -F "image=${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${CI_COMMIT_TAG}" \
    "${CHOERODON_URL}/devops/ci" \
    -o "${CI_COMMIT_SHA}-ci.response" \
    -w %{http_code})
  # 判断本次上传到devops是否出错
  response_upload_to_devops=$(cat "${CI_COMMIT_SHA}-ci.response")
  rm "${CI_COMMIT_SHA}-ci.response"
  if [ "$result_upload_to_devops" != "200" ]; then
    echo $response_upload_to_devops
    echo "upload to devops error"
    exit 1
  fi
}

#################################### 下载settings文件 ####################################
# $1 fileName   下载settings文件后保存为的文件名称
# $2 project_id 项目id
# $3 ciJobId    猪齿鱼的CI的JOB纪录的id
# $4 sequence   猪齿鱼的CI流水线的步骤的序列号
function downloadSettingsFile() {
  rm -rf "$1"
  http_status_code=$(curl -o "$1" -s -m 10 --connect-timeout 10 -w %{http_code} "${CHOERODON_URL}/devops/v1/projects/$2/ci_jobs/maven_settings?job_id=$3&sequence=$4&token=${Token}")

  if [ "$http_status_code" != "200" ]; then
    echo "failed to downloadSettingsFile: $1"
    exit 1
  fi
}
#################################### 触发cd流水线 ####################################
function triggerCdPipeline() {
  http_status_code=$(curl -X POST -o -s -m 10 --connect-timeout 10 -w %{http_code} "${CHOERODON_URL}/devops/v1/cd_pipeline/trigger_cd_pipeline?token=${Token}&commit=${CI_COMMIT_SHA}&ref=${CI_COMMIT_REF_NAME}&gitlab_user_id=${GITLAB_USER_ID}&gitlab_pipeline_id=${CI_PIPELINE_ID}")

  if [ "$http_status_code" != "204" ]; then
    echo "failed to triggerCdPipeline"
    exit 1
  fi
}