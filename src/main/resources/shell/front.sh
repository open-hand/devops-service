export GROUP_NAME={{ GROUP_NAME }}
export PROJECT_NAME={{ PROJECT_NAME }}


CI_COMMIT_YEAR=$(git log -1 --pretty=format:"%cd" --date=format:"%Y")
CI_COMMIT_MONTH=$(git log -1 --pretty=format:"%cd" --date=format:"%m" | sed s'/^0//')
CI_COMMIT_DAY=$(git log -1 --pretty=format:"%cd" --date=format:"%d" | sed s'/^0//')
CI_COMMIT_HOURS=$(git log -1 --pretty=format:"%cd" --date=format:"%H")
CI_COMMIT_MINUTES=$(git log -1 --pretty=format:"%cd" --date=format:"%M")
CI_COMMIT_SECONDS=$(git log -1 --pretty=format:"%cd" --date=format:"%S")
export C7N_GIT_COMMIT_TIME=$CI_COMMIT_YEAR.$CI_COMMIT_MONTH.$CI_COMMIT_DAY-$CI_COMMIT_HOURS$CI_COMMIT_MINUTES$CI_COMMIT_SECONDS

# 8位sha值
export C7N_GIT_COMMIT_SHA=$(git log -1 --pretty=format:"%H" | awk '{print substr($1,1,8)}')

# 分支名
if [ $CIRCLECI ]; then
  export C7N_GIT_BRANCH=$CIRCLE_BRANCH
fi

if [ $GITLAB_CI ]; then
  export C7N_GIT_BRANCH=$CI_COMMIT_REF_NAME
fi

# 默认Version
export C7N_GITVERSION=$C7N_GIT_COMMIT_TIME-$C7N_GIT_BRANCH

export CI_COMMIT_TAG=$C7N_GITVERSION

function node_config(){
    npm config set registry ${NODE_REGISTRY:-"http://nexus3.deploy.saas.hand-china.com/repository/handnpm/"}
}
function node_module(){
    mkdir -p /cache/${CI_PROJECT_NAME}-${CI_PROJECT_ID}-${CI_COMMIT_REF_NAME}-${CI_COMMIT_SHA}
    python ./boot/structure/configAuto.py ${1}
    cp -r config.yml /cache/${CI_PROJECT_NAME}-${CI_PROJECT_ID}-${CI_COMMIT_REF_NAME}-${CI_COMMIT_SHA}/
    cd boot && npm install && cd ../${1} && npm install && cd ..
}
# 开发使用devbuild，构建镜像使用build
function node_build(){
    cd boot
    ./node_modules/.bin/gulp start
    cnpm run ${1:-"build"}
    find dist -name '*.js' | xargs sed -i "s/localhost:version/$CI_COMMIT_TAG/g"
}
function cache_dist(){
    cp -r dist /cache/${CI_PROJECT_NAME}-${CI_PROJECT_ID}-${CI_COMMIT_REF_NAME}-${CI_COMMIT_SHA}/dist
}

function docker_build(){
    cp -r /cache/${CI_PROJECT_NAME}-${CI_PROJECT_ID}-${CI_COMMIT_REF_NAME}-${CI_COMMIT_SHA}/* ${1:-"."}
    docker build --pull -t ${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${CI_COMMIT_TAG} ${1:-"."}
    docker push ${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${CI_COMMIT_TAG}
}
function chart_build(){
    CHART_PATH=`find . -maxdepth 3 -name Chart.yaml`
    sed -i 's/repository:.*$/repository\:\ '${DOCKER_REGISTRY}'\/'${GROUP_NAME}'\/'${PROJECT_NAME}'/g' ${CHART_PATH%/*}/values.yaml
    helm package ${CHART_PATH%/*} --version ${CI_COMMIT_TAG} --app-version ${CI_COMMIT_TAG}
    TEMP=${CHART_PATH%/*}
    FILE_NAME=${TEMP##*/}
    curl -X POST \
        -F "token=${Token}" \
        -F "version=${CI_COMMIT_TAG}" \
        -F "file=@${FILE_NAME}-${CI_COMMIT_TAG}.tgz" \
        -F "commit=${CI_COMMIT_SHA}" \
        -F "image=${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${CI_COMMIT_TAG}" \
        "${CHOERODON_URL}/devops/ci"
    if [ $? -ne 0 ]; then
        echo "upload chart error"
        exit 1
    fi
}
function clean_cache(){
    rm -rf /cache/${CI_PROJECT_NAME}-${CI_PROJECT_ID}-${CI_COMMIT_REF_NAME}-${CI_COMMIT_SHA}
}