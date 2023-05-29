#!/bin/bash
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
# 获取的组织编码-项目编码(harbor Project地址) 也可能是自定一harbor仓库名
export GROUP_NAME={{ GROUP_NAME }}
# 获取的组织编码-项目编码(harbor Project地址)
export SONAR_GROUP_NAME={{ SONAR_GROUP_NAME }}
# 获取的组织编码-项目编码(harbor Project地址)
export SONAR_PROJECT_KEY={{ SONAR_PROJECT_KEY }}
# SONARQUBE的地址
export SONAR_URL={{ SONAR_URL }}
# SONARQUBE的token
export SONAR_LOGIN={{ SONAR_LOGIN }}
# HARBOR仓库类型
export REPO_TYPE={{ REPO_TYPE }}
# HARBOR配置Id
export HARBOR_CONFIG_ID={{ HARBOR_CONFIG_ID }}
# 设置docekr认证配置文件目录
export DOCKER_CONFIG=$PWD/.choerodon/.docker
export CHOERODON_URL={{ CHOERODON_URL }}

# 创建docekr认证配置文件目录
mkdir -p $DOCKER_CONFIG
# 设成docekr认证配置文件
echo "{\"auths\":{\"$DOCKER_REGISTRY\":{\"auth\":\"$(echo -n $DOCKER_USERNAME:$DOCKER_PASSWORD | base64)\"}}}" | tr -d '\n' > $DOCKER_CONFIG/config.json

function export_commit_tag() {
   # 判断是否有git命令，没有退出此函数
   if ! which git > /dev/null;
   then
       echo '未找到 Git 命令行，无法生成 tag。'
       return
   fi

    # 获取commit时间
    C7N_COMMIT_TIMESTAMP=`git log -1 --date=format-local:%Y%m%d%H%M%S --pretty=format:"%cd" || date +%Y%m%d%H%M%S`
    C7N_COMMIT_YEAR=${C7N_COMMIT_TIMESTAMP:0:4}
    C7N_COMMIT_MONTH=$(echo ${C7N_COMMIT_TIMESTAMP:4:2} | sed s'/^0//')
    C7N_COMMIT_DAY=$(echo ${C7N_COMMIT_TIMESTAMP:6:2} | sed s'/^0//')
    C7N_COMMIT_HOURS=${C7N_COMMIT_TIMESTAMP:8:2}
    C7N_COMMIT_MINUTES=${C7N_COMMIT_TIMESTAMP:10:2}
    C7N_COMMIT_SECONDS=${C7N_COMMIT_TIMESTAMP:12:2}
    export C7N_COMMIT_TIME=$C7N_COMMIT_YEAR.$C7N_COMMIT_MONTH.$C7N_COMMIT_DAY-$C7N_COMMIT_HOURS$C7N_COMMIT_MINUTES$C7N_COMMIT_SECONDS

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
      export C7N_VERSION={{ C7N_VERSION_RULE }}  # $C7N_COMMIT_TIME-$C7N_BRANCH
    fi
    export CI_COMMIT_TAG=$C7N_VERSION
}

export_commit_tag

# 参数为要合并的远程分支名,默认 develop
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
  export_commit_tag
  cd boot
  ./node_modules/.bin/gulp start
  cnpm run ${1:-"build"}
  find dist -name '*.js' | xargs sed -i "s/localhost:version/$C7N_VERSION/g"
}

function cache_dist() {
  cp -r dist /cache/${CI_PROJECT_NAMESPACE}-${CI_PROJECT_NAME}-${CI_COMMIT_SHA}/dist
}

#################################### 微服务相关函数 ####################################
# 更新maven项目本版本号
# $1 填入true表示用项目根目录的settings.xml文件，其他任何值都不使用本地settings.xml
function update_pom_version() {
  export_commit_tag
  if [ "$1" == "true" -a -f settings.xml ];then
      echo "Update pom version: using custom settings.xml..."
      mvn versions:set -DnewVersion=${C7N_VERSION} -s settings.xml ||
              find . -name pom.xml | xargs xml ed -L \
                -N x=http://maven.apache.org/POM/4.0.0 \
                -u '/x:project/x:version' -v "${C7N_VERSION}"
      mvn versions:commit -s settings.xml
  else
      echo "Update pom version: using default settings.xml..."
      mvn versions:set -DnewVersion=${C7N_VERSION} ||
        find . -name pom.xml | xargs xml ed -L \
          -N x=http://maven.apache.org/POM/4.0.0 \
          -u '/x:project/x:version' -v "${C7N_VERSION}"
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
  mkdir -p /cache/${CI_PROJECT_NAMESPACE}-${CI_PROJECT_NAME}-${CI_COMMIT_SHA}-jar
  cp target/app.jar  /cache/${CI_PROJECT_NAMESPACE}-${CI_PROJECT_NAME}-${CI_COMMIT_SHA}-jar/app.jar
}

#################################### 构建镜像 ####################################
$1: skipTlsVerify 是否跳过证书校验
$2: dockerBuildContextDir docker构建上下文
$3: dockerFilePath Dockerfile路径
function kaniko_build() {
  export_commit_tag
  if [ -z $KUBERNETES_SERVICE_HOST ];then
      ssh -o StrictHostKeyChecking=no root@kaniko DOCKER_CONFIG=${DOCKER_CONFIG} /kaniko/kaniko $1  --no-push \
      -c $PWD/$2 -f $PWD/$3 -d ${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${C7N_VERSION} \
      --tarPath ${PWD}/${PROJECT_NAME}.tar --force
  else
      ssh -o StrictHostKeyChecking=no root@127.0.0.1 DOCKER_CONFIG=${DOCKER_CONFIG} /kaniko/kaniko $1  --no-push \
      -c $PWD/$2 -f $PWD/$3 -d ${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${C7N_VERSION} \
      --tarPath ${PWD}/${PROJECT_NAME}.tar --force
  fi
}

function skopeo_copy() {
  export_commit_tag
  echo "Pushing image to docker repo, image tag is ${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${C7N_VERSION}";
  skopeo copy --dest-tls-verify=false --dest-creds=${DOCKER_USERNAME}:${DOCKER_PASSWORD} docker-archive:${PWD}/${PROJECT_NAME}.tar docker://${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${C7N_VERSION}
}

#################################### 构建镜像 ####################################
function docker_build() {
  export_commit_tag
  cp /cache/${CI_PROJECT_NAMESPACE}-${CI_PROJECT_NAME}-${CI_COMMIT_SHA}-jar/app.jar ${1:-"src/main/docker"}/app.jar || true
  cp -r /cache/${CI_PROJECT_NAMESPACE}-${CI_PROJECT_NAME}-${CI_COMMIT_SHA}/* ${1:-"."} || true
  docker build -t ${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${C7N_VERSION} ${1:-"."} || true
  docker build -t ${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${C7N_VERSION} ${1:-"src/main/docker"} || true
  docker push ${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${C7N_VERSION}
}

#################################### 清理缓存 ####################################
function clean_cache() {
  rm -rf /cache/${CI_PROJECT_NAMESPACE}-${CI_PROJECT_NAME}-${CI_COMMIT_SHA}-jar
  rm -rf /cache/${CI_PROJECT_NAMESPACE}-${CI_PROJECT_NAME}-${CI_COMMIT_SHA}
}

################################ 上传生成的chart包到猪齿鱼平台的devops-service ##################################
# 此项为上传构建并上传chart包到Choerodon中，只有通过此函数Choerodon才会有相应版本记录。
# $1 helm 仓库id
function chart_build() {
  export_commit_tag
  rewrite_image_info_for_chart
  # 查找Chart.yaml文件
  CHART_PATH=$(find . -maxdepth 3 -name Chart.yaml)
  # 重置values.yaml文件中image属性
  if [ $(grep registry: ${CHART_PATH%/*}/values.yaml -c | cat) -eq 0 ]; then
    sed -i "s,repository:.*$,repository: ${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME},g" \
      ${CHART_PATH%/*}/values.yaml
  else
    which yq > /dev/null || echo "cibase不包含yq指令，请升级"
    export DOCKER_REPOSITORY="${GROUP_NAME}/${PROJECT_NAME}"
    yq e -i '.image.registry=strenv(DOCKER_REGISTRY)' ${CHART_PATH%/*}/values.yaml
    yq e -i '.image.repository=strenv(DOCKER_REPOSITORY)' ${CHART_PATH%/*}/values.yaml
  fi
  nameInChart=$(grep -e '^name:' ${CHART_PATH%/*}/Chart.yaml| awk '{print $2}')
  # 重置Chart.yaml中的name属性，与当前应用的code保持一致
  if [ $nameInChart != ${PROJECT_NAME} ]; then
    echo "Rewrite chart name from ${nameInChart} to ${PROJECT_NAME} "
    sed -i "s,^name:.*$,name: ${PROJECT_NAME},g" ${CHART_PATH%/*}/Chart.yaml
  fi
  # 构建chart包，重写version与app-version为当前版本
  helm package -u ${CHART_PATH%/*} --version ${C7N_VERSION} --app-version ${C7N_VERSION}
  TEMP=${CHART_PATH%/*}
  FILE_NAME=${TEMP##*/}
  # 通过Choerodon API上传chart包到devops-service
  result_upload_to_devops=$(curl -X POST \
    -H 'Expect:' \
    -F "token=${Token}" \
    -F "harbor_config_id=${HARBOR_CONFIG_ID}" \
    -F "repo_type=${REPO_TYPE}" \
    -F "version=${C7N_VERSION}" \
    -F "file=@${PROJECT_NAME}-${C7N_VERSION}.tgz" \
    -F "commit=${CI_COMMIT_SHA}" \
    -F "ref=${CI_COMMIT_REF_NAME}" \
    -F "gitlabPipelineId=${CI_PIPELINE_ID}" \
    -F "jobName=${CI_JOB_NAME}" \
    -F "image=${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${C7N_VERSION}" \
    -F "helm_repo_id=$1" \
    -F "gitlab_user_id=${GITLAB_USER_ID}" \
    "${CHOERODON_URL}/devops/ci" \
    -o "${CI_COMMIT_SHA}-ci.response" \
    -w %{http_code})
  # 判断本次上传到devops是否出错
  if [ -e "${CI_COMMIT_SHA}-ci.response" ]; then
    response_upload_to_devops=$(cat "${CI_COMMIT_SHA}-ci.response")
    rm "${CI_COMMIT_SHA}-ci.response"
    if [ "$result_upload_to_devops" != "200" ]; then
      echo $response_upload_to_devops
      echo "upload to devops error"
      exit 1
    fi
  fi

}

#################################### 发布应用服务版本 ####################################
function publish_app_version() {
  export_commit_tag
  # 通过Choerodon API上传chart包到devops-service
  result_upload_to_devops=$(curl -X POST \
    -H 'Expect:' \
    -F "token=${Token}" \
    -F "version=${C7N_VERSION}" \
    -F "commit=${CI_COMMIT_SHA}" \
    -F "ref=${CI_COMMIT_REF_NAME}" \
    -F "gitlabPipelineId=${CI_PIPELINE_ID}" \
    -F "jobName=${CI_JOB_NAME}" \
    "${CHOERODON_URL}/devops/ci/app_version" \
    -o "${CI_COMMIT_SHA}-ci.response" \
    -w %{http_code})
  # 判断本次上传到devops是否出错
  if [ -e "${CI_COMMIT_SHA}-ci.response" ]; then
    response_upload_to_devops=$(cat "${CI_COMMIT_SHA}-ci.response")
    rm "${CI_COMMIT_SHA}-ci.response"
    if [ "$result_upload_to_devops" != "200" ]; then
      echo $response_upload_to_devops
      echo "upload to devops error"
      exit 1
    fi
  fi

}
#################################### 下载settings文件 ####################################
# $1 projectId
# $2 devops_ci_maven_settings.id
function downloadSettingsFileByUId() {
  rm -rf "settings.xml"
  http_status_code=$(curl -o "settings.xml" -s -m 10 --connect-timeout 10 -w %{http_code} "${CHOERODON_URL}/devops/v1/projects/$1/ci_jobs/maven_settings/$2?token=${Token}")

  if [ "$http_status_code" != "200" ]; then
    cat "settings.xml"
    echo "failed to downloadSettingsFile: $1"
    exit 1
  fi
}

#################################### 下载配置文件 ####################################
# $1 config_file_id
# $2 config_file_path
function downloadConfigFileByUId() {
  http_status_code=$(curl -o result.json -s -m 10 --connect-timeout 10 -w %{http_code} "${CHOERODON_URL}/devops/ci/config_file/?token=${Token}&config_file_id=${1}")
  if [ "$http_status_code" != "200" ];
    then
      echo "Download Configfile failed."
      exit 1
  else
    # 保存到其他目录，需要提前创建目录
    if [[ "$2" =~ "/" ]]; then
      mkdir -p $(dirname "$2")
    fi
    # 输出配置文件到目标路径
    mv result.json "$2"
  fi
}


############################### 存储镜像元数据, 用于CD阶段主机部署-镜像部署 ################################
# 无参数
# 此函数上传镜像构建元数据, 只有任务(job)通过这个函数上传了镜像元数据,
# 在CD阶段的主机部署-镜像部署中选中了这个任务(job)的任务才能正确部署镜像
function saveImageMetadata() {
    export_commit_tag
    result_upload_to_devops=$(curl -X POST "${CHOERODON_URL}/devops/ci/record_image" \
      --header 'Content-Type: application/json' \
      -d "{
        \"token\": \"${Token}\",
        \"gitlabPipelineId\": ${CI_PIPELINE_ID},
        \"jobName\": \"${CI_JOB_NAME}\",
        \"imageTag\": \"${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${C7N_VERSION}\",
        \"harborRepoId\": ${HARBOR_CONFIG_ID},
        \"repoType\": \"${REPO_TYPE}\",
        \"version\": \"${C7N_VERSION}\"
      }" \
      -o "${CI_COMMIT_SHA}-ci.response" \
      -w %{http_code})

    # 判断本次上传到devops是否出错
    response_upload_to_devops=$(cat "${CI_COMMIT_SHA}-ci.response")
    rm "${CI_COMMIT_SHA}-ci.response"
    if [ "$result_upload_to_devops" != "200" ]; then
      echo "${response_upload_to_devops}"
      echo "upload to devops error"
      exit 1
    fi
}

############################### 存储jar包元数据, 用于CD阶段主机部署-jar包部署 ################################
# $1 maven制品库id
# $2 mvn_settings_id    mvn_settings_id
# $3 sequence   猪齿鱼的CI流水线的步骤的序列号
function saveJarInfo() {
  export_commit_tag
  result_upload_to_devops=""
  if [ -n "${CHOERODON_MAVEN_POM_LOCATION}" ]; then
      result_upload_to_devops=$(curl -X POST \
          -H 'Expect:' \
          -F "token=${Token}" \
          -F "nexus_repo_id=$1" \
          -F "sequence=$2" \
          -F "gitlab_pipeline_id=${CI_PIPELINE_ID}" \
          -F "job_name=${CI_JOB_NAME}" \
          -F "version=${C7N_VERSION}" \
          -F "file=@pom.xml" \
          "${CHOERODON_URL}/devops/ci/save_jar_info" \
          -o "${CI_COMMIT_SHA}-ci.response" \
          -w %{http_code})
    else
      result_upload_to_devops=$(curl -X POST \
                -H 'Expect:' \
                -F "token=${Token}" \
                -F "nexus_repo_id=$1" \
                -F "sequence=$2" \
                -F "gitlab_pipeline_id=${CI_PIPELINE_ID}" \
                -F "job_name=${CI_JOB_NAME}" \
                -F "version=${C7N_VERSION}" \
                -F "group_id=${CHOERODON_MAVEN_GROUP_ID}" \
                -F "artifact_id=${CHOERODON_MAVEN_ARTIFACT_ID}" \
                -F "jar_version=${CHOERODON_MAVEN_VERSION}" \
                -F "packaging=${CHOERODON_MAVEN_PACKAGING}" \
                "${CHOERODON_URL}/devops/ci/save_jar_info" \
                -o "${CI_COMMIT_SHA}-ci.response" \
                -w %{http_code})
  fi

  # 判断本次上传到devops是否出错
  if [ -e "${CI_COMMIT_SHA}-ci.response" ]; then
    response_upload_to_devops=$(cat "${CI_COMMIT_SHA}-ci.response")
    rm "${CI_COMMIT_SHA}-ci.response"
    if [ "$result_upload_to_devops" != "200" ]; then
      echo $response_upload_to_devops
      echo "upload to devops error"
      exit 1
    fi
  fi
}
############################### 存储jar包元数据, 用于CD阶段主机部署-jar包部署 ################################
# $1 mvn_settings_id   mvn_settings_id
# $2 sequence   猪齿鱼的CI流水线的步骤的序列号
# $3 maven_repo_url   目标仓库地址
# $4 username   目标仓库用户名
# $5 password   目标仓库用户密码
function saveCustomJarInfo() {
  export_commit_tag
  result_upload_to_devops=""
  if [ -n "${CHOERODON_MAVEN_POM_LOCATION}" ]; then
    result_upload_to_devops=$(curl -X POST \
        -H 'Expect:' \
        -F "token=${Token}" \
        -F "sequence=$1" \
        -F "maven_repo_url=$2" \
        -F "username='$3'" \
        -F "password='$4'" \
        -F "gitlab_pipeline_id=${CI_PIPELINE_ID}" \
        -F "job_name=${CI_JOB_NAME}" \
        -F "version=${C7N_VERSION}" \
        -F "file=@pom.xml" \
        "${CHOERODON_URL}/devops/ci/save_jar_info" \
        -o "${CI_COMMIT_SHA}-ci.response" \
        -w %{http_code})
  else
    result_upload_to_devops=$(curl -X POST \
            -H 'Expect:' \
            -F "token=${Token}" \
            -F "sequence=$1" \
            -F "maven_repo_url=$2" \
            -F "username='$3'" \
            -F "password='$4'" \
            -F "gitlab_pipeline_id=${CI_PIPELINE_ID}" \
            -F "job_name=${CI_JOB_NAME}" \
            -F "version=${C7N_VERSION}" \
            -F "groupId${CHOERODON_MAVEN_GROUP_ID}" \
            -F "artifactId=${CHOERODON_MAVEN_ARTIFACT_ID}" \
            -F "jarVersion=${CHOERODON_MAVEN_VERSION}" \
            -F "packaging=${CHOERODON_MAVEN_PACKAGING}" \
            "${CHOERODON_URL}/devops/ci/save_jar_info" \
            -o "${CI_COMMIT_SHA}-ci.response" \
            -w %{http_code})
  fi

  # 判断本次上传到devops是否出错
  if [ -e "${CI_COMMIT_SHA}-ci.response" ]; then
    response_upload_to_devops=$(cat "${CI_COMMIT_SHA}-ci.response")
    rm "${CI_COMMIT_SHA}-ci.response"
    if [ "$result_upload_to_devops" != "200" ]; then
      echo $response_upload_to_devops
      echo "upload to devops error"
      exit 1
    fi
  fi
}

############################### 检查sonar扫描的信息，并根据质量门设置，判断是否终止job ################################
function checkSonarQualityGateScanResult(){
  if [ "$1" == "true " ]; then
    response_code=$(curl -X GET \
        -F "token=${Token}" \
        -F "gitlab_pipeline_id=${CI_PIPELINE_ID}" \
        "${CHOERODON_URL}/devops/ci/get_sonar_quality_gate_result" \
        -o "sonarQualityGateScanResult.json" \
        -w %{http_code})

    if [ "${response_code}" != 200 ]; then
         cat sonarQualityGateScanResult.json
         exit 1
    fi

    scan_result=$(cat sonarQualityGateScanResult.json)
    if [ "${scan_result}" != true ]; then
        echo "The sonar quality gate doesn't pass"
        exit 1
    fi
  fi
}


############################### 存储sonar扫描的信息 ################################
# $1 scanner_type 扫描器类型
function saveSonarInfo() {
  result_upload_to_devops=$(curl -X POST \
    -H 'Expect:' \
    -F "token=${Token}" \
    -F "gitlab_pipeline_id=${CI_PIPELINE_ID}" \
    -F "job_name=${CI_JOB_NAME}" \
    -F "scanner_type=$1" \
    "${CHOERODON_URL}/devops/ci/save_sonar_info" \
    -o "${CI_COMMIT_SHA}-ci.response" \
    -w %{http_code})
  # 判断本次上传到devops是否出错
  response_upload_to_devops=$(cat "${CI_COMMIT_SHA}-ci.response")
  rm "${CI_COMMIT_SHA}-ci.response"
  if [ "$result_upload_to_devops" != "200" ]; then
    echo "$response_upload_to_devops"
    echo "upload to devops error"
    exit 1
  fi
}

############################### 解析ci阶段镜像扫描产生的json文件，存于数据库 ###############################
# $2 ciJobId    猪齿鱼的CI的JOB的id
function trivyScanImage() {
  export_commit_tag
  which trivy > /dev/null || echo "cibase不包含trivy指令，请升级"
  which ssh > /dev/null || echo "cibase不包含ssh指令，请升级"
  export TRIVY_INSECURE='true'
  startDate=$(date +"%Y-%m-%d %H:%M:%S")
  trivy image  --skip-update -f json -o results-${C7N_VERSION}.json  --input ${PWD}/${PROJECT_NAME}.tar
  endDate=$(date +"%Y-%m-%d %H:%M:%S")
  upload_trivy_sacn_result
}

function trivyScanImageForDocker() {
  export_commit_tag
  which trivy > /dev/null || echo "cibase不包含trivy指令，请升级"
  which ssh > /dev/null || echo "cibase不包含ssh指令，请升级"
  export TRIVY_INSECURE='true'
  startDate=$(date +"%Y-%m-%d %H:%M:%S")
  trivy image --skip-update -f json -o results-${C7N_VERSION}.json ${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${C7N_VERSION}
  endDate=$(date +"%Y-%m-%d %H:%M:%S")
  upload_trivy_sacn_result
}
function upload_trivy_sacn_result() {
  export_commit_tag
  result_upload_to_devops=$(curl -X POST \
  -H 'Expect:' \
  -F "gitlab_pipeline_id=${CI_PIPELINE_ID}" \
  -F "job_id=$1" \
  -F "token=${Token}" \
  -F "job_name=${CI_JOB_NAME}" \
  -F "start_date=${startDate}" \
  -F "end_date=${endDate}" \
  -F "file=@results-${C7N_VERSION}.json" \
  "${CHOERODON_URL}/devops/ci/resolve_image_scan_json" \
  -o "${CI_COMMIT_SHA}-ci.response" \
  -w %{http_code})
  # 判断本次上传到devops是否出错
  response_upload_to_devops=$(cat "${CI_COMMIT_SHA}-ci.response")
  rm "${CI_COMMIT_SHA}-ci.response"
  if [ "$result_upload_to_devops" != "200" ]; then
    echo "$response_upload_to_devops"
    echo "upload to devops error"
    exit 1
  fi
}

# 上传maven单元测试报告
function uploadMavenUnitTestReport() {
    uploadUnitTestReport maven_unit_test target/site/surefire-report.html
}
# 上传go单元测试报告
function uploadGoUnitTestReport() {
    uploadUnitTestReport go_unit_test result.xml
}
# 上传nodeJs单元测试报告
function uploadNodeJsUnitTestReport() {
    tar -cvf report.zip mochawesome-report/
    uploadUnitTestReport node_js_unit_test report.zip
}

# 上传通用单元测试报告
# $1 测试报告路径
# $2 测试用例总数
# $3 测试用例通过数
# $4 测试用例失败数
# $5 测试用例跳过数
function uploadGeneralUnitTestReport() {
    uploadUnitTestReport general_unit_test $1 $2 $3 $4 $5
}


# 上传测试报告
# $1 测试报告类型
# $2 测试报告路径
# $3 测试用例总数
# $4 测试用例通过数
# $5 测试用例失败数
# $6 测试用例跳过数
function uploadUnitTestReport() {
    result_upload_to_devops=$(curl -X POST \
    -H 'Expect:' \
    -F "gitlab_pipeline_id=${CI_PIPELINE_ID}" \
    -F "token=${Token}" \
    -F "job_name=${CI_JOB_NAME}" \
    -F "type=$1" \
    -F "file=@$2" \
    -F "tests=$3" \
    -F "passes=$4" \
    -F "failures=$5" \
    -F "skipped=$6" \
    "${CHOERODON_URL}/devops/ci/upload_unit_test" \
    -o "${CI_COMMIT_SHA}-ci.response" \
    -w %{http_code})
  # 判断本次上传到devops是否出错
  response_upload_to_devops=$(cat "${CI_COMMIT_SHA}-ci.response")
  rm "${CI_COMMIT_SHA}-ci.response"
  if [ "$result_upload_to_devops" != "200" ]; then
    echo "$response_upload_to_devops"
    echo "upload to devops error"
    exit 1
  fi
}
# $1 是否跳过单元测试
function mvnCompile() {
    if [ -z $KUBERNETES_SERVICE_HOST ];then
        ssh -o StrictHostKeyChecking=no root@kaniko "cd $PWD && JAVA_HOME=/opt/java/openjdk PATH=/opt/java/openjdk/bin:$PATH mvn --batch-mode clean org.jacoco:jacoco-maven-plugin:prepare-agent install  -Dmaven.test.failure.ignore=true -DskipTests=$1"
    else
        ssh -o StrictHostKeyChecking=no root@127.0.0.1 "cd $PWD && JAVA_HOME=/opt/java/openjdk PATH=/opt/java/openjdk/bin:$PATH mvn --batch-mode clean org.jacoco:jacoco-maven-plugin:prepare-agent install  -Dmaven.test.failure.ignore=true -DskipTests=$1"
    fi
}

# $1 是否跳过单元测试
function mvnCompileUseSettings() {
    if [ -z $KUBERNETES_SERVICE_HOST ];then
        ssh -o StrictHostKeyChecking=no root@kaniko "cd $PWD && JAVA_HOME=/opt/java/openjdk PATH=/opt/java/openjdk/bin:$PATH mvn --batch-mode clean org.jacoco:jacoco-maven-plugin:prepare-agent install  -Dmaven.test.failure.ignore=true -DskipTests=$1 -s settings.xml"
    else
        ssh -o StrictHostKeyChecking=no root@127.0.0.1 "cd $PWD && JAVA_HOME=/opt/java/openjdk PATH=/opt/java/openjdk/bin:$PATH mvn --batch-mode clean org.jacoco:jacoco-maven-plugin:prepare-agent install  -Dmaven.test.failure.ignore=true -DskipTests=$1 -s settings.xml"
    fi
}
# 重新镜像上传相关变量，用于控制推送到不同镜像仓库
# $1 projectId
# $2 repoType
# $3 repoId
function rewrite_image_info() {
  http_status_code=$(curl -o rewrite_image_info.json -s -m 10 --connect-timeout 10 -w %{http_code} "${CHOERODON_URL}/devops/ci/rewrite_repo_info_script?token=${Token}&project_id=$1&repo_type=$2&repo_id=$3")
  echo "Query repo info status code is :"  $http_status_code
  if [ "$http_status_code" != "200" ];
  then
    cat rewrite_image_info.json
    echo "Query repo info failed,skip rewrite image info"
  else
    is_failed=$(jq -r .failed rewrite_image_info.json)
    if [ "${is_failed}" == "true" ];
    then
      cat rewrite_image_info.json
      echo "Query chart repo info failed,skip rewrite image info"
    else
      export DOCKER_REGISTRY=$(jq -r .dockerRegistry rewrite_image_info.json)
      export GROUP_NAME=$(jq -r .groupName rewrite_image_info.json)
      export DOCKER_USERNAME=$(jq -r .dockerUsername rewrite_image_info.json)
      export HARBOR_CONFIG_ID=$(jq -r .harborRepoId rewrite_image_info.json)
      export REPO_TYPE=$(jq -r .repoType rewrite_image_info.json)
      export DOCKER_PASSWORD=$(jq -r .dockerPassword rewrite_image_info.json)
      # 设成docekr认证配置文件
      echo "{\"auths\":{\"$DOCKER_REGISTRY\":{\"auth\":\"$(echo -n $DOCKER_USERNAME:$DOCKER_PASSWORD | base64)\"}}}" | tr -d '\n' > $DOCKER_CONFIG/config.json
    fi
  fi

}

function rewrite_image_info_for_chart() {
  echo "Query chart image repo info"
  http_status_code=$(curl -o rewrite_image_info.json -s -m 10 --connect-timeout 10 -w %{http_code} "${CHOERODON_URL}/devops/ci/image_repo_info?token=${Token}&gitlab_pipeline_id=${CI_PIPELINE_ID}")
  echo "Query chart repo info status code is :"  $http_status_code
  if [ "$http_status_code" != "200" ];
  then
    echo "Query chart repo info failed,skip rewrite image info"
  else
    is_failed=$(jq -r .failed rewrite_image_info.json)
    if [ "${is_failed}" == "true" ];
    then
      echo "Query chart repo info failed,skip rewrite image info"
    else
      export DOCKER_REGISTRY=$(jq -r .dockerRegistry rewrite_image_info.json)
      export GROUP_NAME=$(jq -r .groupName rewrite_image_info.json)
      export HARBOR_CONFIG_ID=$(jq -r .harborRepoId rewrite_image_info.json)
      export REPO_TYPE=$(jq -r .repoType rewrite_image_info.json)
    fi
  fi

}
## chart部署
## $1 部署配置id
function chart_deploy() {
  app_deploy "${1}" chart_deploy
}

## chart部署
## $1 部署配置id
function deployment_deploy() {
  app_deploy "${1}" deployment_deploy
}
## $1 部署配置id
## $2 指令类型
function app_deploy() {
  http_status_code=$(curl -X POST \
    -H 'Expect:' \
    -F "token=${Token}" \
    -F "gitlab_pipeline_id=${CI_PIPELINE_ID}" \
    -F "gitlab_job_id=${CI_JOB_ID}" \
    -F "config_id=$1" \
    -F "command_type=$2" \
    "${CHOERODON_URL}/devops/ci/exec_command" \
    -o "result.json" \
    -s \
    -w %{http_code})
  if [ "$http_status_code" != "200" ];
  then
    echo "Deploy failed."
    exit 1
  else
    is_failed=$(jq -r .failed result.json)
    message=$(jq -r .message result.json)
    # 打印后台返回的日志
    if [ -n "${message}" ]; then
        echo "${message}"
    fi
    # 判断是否成功
    if [ "${is_failed}" == "true" ];
    then
      echo "Deploy failed"
      exit 1
    fi
  fi
}

# 部署主机应用
## $1 部署配置id
## $2 指令类型
function host_deploy(){
    http_status_code=$(curl -o result.json -X POST -s -m 10 --connect-timeout 10 -w %{http_code} "${CHOERODON_URL}/devops/ci/exec_command?token=${Token}&gitlab_pipeline_id=${CI_PIPELINE_ID}&gitlab_job_id=${CI_JOB_ID}&config_id=$1&command_type=$2")
    if [ "$http_status_code" != "200" ];
    then
      echo "Deploy failed."
      exit 1
    else
      is_failed=$(jq -r .failed result.json)
      message=$(jq -r .message result.json)
      command_id=$(jq -r .content.commandId result.json)
      # 打印后台返回的日志
      if [ -n "${message}" ]; then
          echo "${message}"
      fi

      # 判断是否成功
      if [ "${is_failed}" == "true" ];then
        echo "Deploy failed"
        exit 1
      else
        host_deploy_status_check ${command_id}
      fi
    fi
}

# 检查主机应用部署命令执行结果
function host_deploy_status_check() {
    while :
    do
      echo "等待agent执行部署命令..."
      sleep 5s
      http_status_code=$(curl -o result.json -X POST -s -m 10 --connect-timeout 10 -w %{http_code} "${CHOERODON_URL}/devops/ci/host_command_status?token=${Token}&gitlab_pipeline_id=${CI_PIPELINE_ID}&command_id=$1")
      if [ "$http_status_code" != "200" ];
      then
        echo "Failed to check deploy status.HttpStatusCode is ${http_status_code}.Response is:\n"
        cat result.json
        exit 1
      else
        is_failed=$(jq -r .failed result.json)
        status=$(jq -r .content.status result.json)
        error_msg=$(jq -r .content.errorMsg result.json)
        if [ "${is_failed}" == "true" ];then
          echo "部署失败"
          echo "$error_msg"
          exit 1
        else
          if [ "${status}" == "success" ]; then
              echo "部署成功"
              echo "$error_msg"
              exit 0
          fi
        fi
      fi
    done
}

## 执行人工审核任务
function process_audit() {
   http_status_code=$(curl -X POST \
    -H 'Expect:' \
    -F "token=${Token}" \
    -F "gitlab_pipeline_id=${CI_PIPELINE_ID}" \
    -F "job_name=${CI_JOB_NAME}" \
    "${CHOERODON_URL}/devops/ci/audit_status" \
    -o "result.json" \
    -s \
    -w %{http_code})
  if [ "$http_status_code" != "200" ];
  then
    echo "audit failed."
    exit 1
  else
    if [ "$(jq -r .countersigned result.json)" == "true" ];
      then
        echo "审核模式为：会签"
      else
        echo "审核模式为：或签"
    fi
    echo "审核通过人员：$(jq -r '.passedUserNameList | join(",")' result.json)"
    echo "审核拒绝人员：$(jq -r '.refusedUserNameList | join(",")' result.json)"
    echo "未审核人员：$(jq -r '.notAuditUserNameList | join(",")' result.json)"
    if [ "$(jq -r .success result.json)" == "true" ];
    then
        echo "审核结果：通过"
    else
        echo "审核结果：终止"
        exit 1
    fi
  fi
}

function execute_api_test(){
  # apiTestInfoConfigId,这里的configId是测试任务关联的任务配置id
 java -jar /choerodon/app.jar
}

# $1 npm repo id
function export_npm_push_variable() {
echo "Query npm repo info"
  http_status_code=$(curl -o npm_repo_info.json -s -m 10 --connect-timeout 10 -w %{http_code} "${CHOERODON_URL}/devops/ci/npm_repo_info?token=${Token}&repo_id=$1")
  echo "Query npm repo info status code is :"  $http_status_code
  if [ "$http_status_code" != "200" ];
  then
    echo "Query npm repo info failed,skip export npm push variable"
  else
    is_failed=$(jq -r .failed npm_repo_info.json)
    if [ "${is_failed}" == "true" ];
    then
      echo "Query npm repo info failed,skip export npm push variable"
    else
      export NPM_REGISTRY=$(jq -r .registry npm_repo_info.json)
      export NPM_USERNAME=$(jq -r .username npm_repo_info.json)
      export NPM_PASSWORD=$(jq -r .password npm_repo_info.json)
      export NPM_EMAIL=$(jq -r .email npm_repo_info.json)
    fi
  fi
}

# $1 pipeline_trigger_config_id $2 ref_name $3 gitlab_project_id $4 token $5 variables
function pipeline_trigger(){
  # 请求gitlab触发流水线
  http_status_code=$(curl -o trigger_result.json -s -m 10 --connect-timeout 10 -w %{http_code}  -X POST "${CI_SERVER_URL}/api/v4/projects/$3/trigger/pipeline?ref=$2&token=$4&$5")
  if [ "$http_status_code" != "201" ];then
    echo "failed to trigger pipeline"
    cat trigger_result.json
    exit 1
  fi
}