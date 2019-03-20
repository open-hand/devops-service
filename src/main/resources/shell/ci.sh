# 获取的组织编码-项目编码
export GROUP_NAME={{ GROUP_NAME }}
# 获取的组织编码
export ORG_CODE={{ ORG_CODE }}
# 获取的项目编码
export PRO_CODE={{ PRO_CODE }}
# 获取的应用名称
export PROJECT_NAME={{ PROJECT_NAME }}


C7N_COMMIT_TIMESTAMP=$(git log -1 --pretty=format:"%ci"| awk '{print $1$2}' | sed 's/[-:]//g')
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


# 更新maven项目本版本号
function update_pom_version(){
    mvn versions:set -DnewVersion=${CI_COMMIT_TAG} || \
    find . -name pom.xml | xargs xml ed -L \
        -N x=http://maven.apache.org/POM/4.0.0 \
        -u '/x:project/x:version' -v "${CI_COMMIT_TAG}"
    mvn versions:commit
}
# 测试分支合并到指定分支，比如合并到master：git_merge master，默认合并到develop
function git_merge(){
    git config user.name ${GITLAB_USER_NAME}
    git config user.email ${GITLAB_USER_EMAIL}
    git checkout origin/${1:-"develop"}
    git merge ${CI_COMMIT_SHA} --no-commit --no-ff
}
# 此项为上传构建并上传chart包到Choerodon中，只有通过此函数Choerodon才会有相应版本记录。
function chart_build(){
    #判断chart主目录名是否与应用编码保持一致
    CHART_DIRECTORY_PATH=`find . -maxdepth 2 -name ${PROJECT_NAME}`
    if [ ! -n "${CHART_DIRECTORY_PATH}" ]; then
        echo "The chart's home directory should be consistent with the application code!"
        exit 1
    fi
    # 查找Chart.yaml文件
    CHART_PATH=`find . -maxdepth 3 -name Chart.yaml`
    # 重置values.yaml文件中image.repository属性
    sed -i "s,repository:.*$,repository: ${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME},g" ${CHART_PATH%/*}/values.yaml
    # 构建chart包，重写version与app-version为当前版本
    helm package ${CHART_PATH%/*} --version ${CI_COMMIT_TAG} --app-version ${CI_COMMIT_TAG}
    TEMP=${CHART_PATH%/*}
    FILE_NAME=${TEMP##*/}
    # 通过Choerodon API上传chart包到devops-service
    result_upload_to_devops=`curl -X POST \
        -F "token=${Token}" \
        -F "version=${CI_COMMIT_TAG}" \
        -F "file=@${FILE_NAME}-${CI_COMMIT_TAG}.tgz" \
        -F "commit=${CI_COMMIT_SHA}" \
        -F "image=${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${CI_COMMIT_TAG}" \
        "${CHOERODON_URL}/devops/ci" \
        -o "${CI_COMMIT_SHA}-ci.response" \
        -w %{http_code}`
    # 判断本次上传到devops是否出错
    response_upload_to_devops=`cat "${CI_COMMIT_SHA}-ci.response"`
    rm "${CI_COMMIT_SHA}-ci.response"
    if [ "$result_upload_to_devops" != "200" ]; then
        echo $response_upload_to_devops
        echo "upload to devops error"
        exit 1
    fi
     # 通过Chartmusume API上传chart包到chart仓库
    result_upload_to_chart=`curl -X POST \
            --data-binary "@${FILE_NAME}-${CI_COMMIT_TAG}.tgz" \
            "${CHART_REGISTRY}/${ORG_CODE}/${PRO_CODE}/api/charts" \
            -o "${CI_COMMIT_SHA}-chart.response" \
            -w %{http_code}`
    response_upload_chart_content=`cat "${CI_COMMIT_SHA}-chart.response"`
    rm "${CI_COMMIT_SHA}-chart.response"
    # 判断本次上传到chartmusume是否出错
    if [ "$result_upload_to_chart" != "201" ]; then
        echo $response_upload_chart_content
        echo "upload to chart error"
        exit 1
    fi
}