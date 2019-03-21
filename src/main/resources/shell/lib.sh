# 获取的组织编码
export ORG_CODE={{ ORG_CODE }}
# 获取的项目编码
export PRO_CODE={{ PRO_CODE }}
# 获取的应用名称
export PROJECT_NAME={{ PROJECT_NAME }}
# 应用Chart仓库地址
export CHART_REGISTRY={{ CHART_REGISTRY }}
# 应用harbor仓库地址
export DOCKER_REGISTRY={{ DOCKER_REGISTRY }}
# 应用harbor仓库用户名
export DOCKER_USERNAME={{ DOCKER_USERNAME }}
# 应用harbor仓库密码
export DOCKER_PASSWORD={{ DOCKER_PASSWORD }}
# 获取的组织编码-项目编码(harbor Project地址)
export GROUP_NAME={{ GROUP_NAME }}

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

function update_pom_version(){
    mvn versions:set -DnewVersion=${CI_COMMIT_TAG} || \
    find . -name pom.xml | xargs xml ed -L \
        -N x=http://maven.apache.org/POM/4.0.0 \
        -u '/x:project/x:version' -v "${CI_COMMIT_TAG}"
    mvn versions:commit
}