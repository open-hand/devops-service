export GROUP_NAME={{ GROUP_NAME }}
export PROJECT_NAME={{ PROJECT_NAME }}
export CI_COMMIT_TAG=$(GetVersion)
function node_config(){
    echo ""
}
function node_module(){
    mkdir -p /cache/${CI_PROJECT_NAME}-${CI_PROJECT_ID}-${CI_COMMIT_REF_NAME}-${CI_COMMIT_SHA}
    python ./boot/structure/configAuto.py ${1}
    cp -r config.yml /cache/${CI_PROJECT_NAME}-${CI_PROJECT_ID}-${CI_COMMIT_REF_NAME}-${CI_COMMIT_SHA}/
    cd boot && cnpm install && cd ../${1} && cnpm install && cd ..
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
    yq w -i ${CHART_PATH%/*}/values.yaml image.repository ${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}
    yq w -i ${CHART_PATH%/*}/values.yaml image.tag ${CI_COMMIT_TAG}
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