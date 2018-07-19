export GROUP_NAME={{ GROUP_NAME }}
export PROJECT_NAME={{ PROJECT_NAME }}


C7N_COMMIT_YEAR=$(git log -1 --pretty=format:"%cd" --date=format:"%Y")
C7N_COMMIT_MONTH=$(git log -1 --pretty=format:"%cd" --date=format:"%m" | sed s'/^0//')
C7N_COMMIT_DAY=$(git log -1 --pretty=format:"%cd" --date=format:"%d" | sed s'/^0//')
C7N_COMMIT_HOURS=$(git log -1 --pretty=format:"%cd" --date=format:"%H")
C7N_COMMIT_MINUTES=$(git log -1 --pretty=format:"%cd" --date=format:"%M")
C7N_COMMIT_SECONDS=$(git log -1 --pretty=format:"%cd" --date=format:"%S")
export C7N_COMMIT_TIME=$C7N_COMMIT_YEAR.$C7N_COMMIT_MONTH.$C7N_COMMIT_DAY-$C7N_COMMIT_HOURS$C7N_COMMIT_MINUTES$C7N_COMMIT_SECONDS

# 8位sha值
export C7N_COMMIT_SHA=$(git log -1 --pretty=format:"%H" | awk '{print substr($1,1,8)}')

# 分支名
if [ $CIRCLECI ]; then
  export C7N_BRANCH=$CIRCLE_BRANCH
elif [ $GITLAB_CI ]; then
  export C7N_BRANCH=$CI_COMMIT_REF_NAME
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

function database_test(){
    while ! mysqlcheck --host=127.0.0.1 --user=root --password=${MYSQL_ROOT_PASSWORD} mysql; do sleep 1; done
    echo "CREATE DATABASE hap_cloud_test DEFAULT CHARACTER SET utf8;" | \
        mysql --user=root --password=${MYSQL_ROOT_PASSWORD} --host=127.0.0.1
    java -Dspring.datasource.url="jdbc:mysql://127.0.0.1/hap_cloud_test?useUnicode=true&characterEncoding=utf-8&useSSL=false" \
        -Dspring.datasource.username=root \
        -Dspring.datasource.password=${MYSQL_ROOT_PASSWORD} \
        -Ddata.dir=src/main/resources \
        -jar /var/hapcloud/hap-liquibase-tool.jar
}
function update_pom_version(){
    mvn versions:set -DnewVersion=${CI_COMMIT_TAG} || \
    find . -name pom.xml | xargs xml ed -L \
        -N x=http://maven.apache.org/POM/4.0.0 \
        -u '/x:project/x:version' -v "${CI_COMMIT_TAG}"
    mvn versions:commit
}
# 参数为要合并的远程分支名,默认develop
# e.g. git_merge develop
function git_merge(){
    git config user.name ${GITLAB_USER_NAME}
    git config user.email ${GITLAB_USER_EMAIL}
    git checkout origin/${1:-"develop"}
    git merge ${CI_COMMIT_SHA} --no-commit --no-ff
}
function cache_jar(){
    mkdir -p ${HOME}/.m2/${GROUP_NAME}/${CI_COMMIT_SHA}
    cp target/app.jar ${HOME}/.m2/${GROUP_NAME}/${CI_COMMIT_SHA}/app.jar
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
function docker_build(){
    cp ${HOME}/.m2/${GROUP_NAME}/${CI_COMMIT_SHA}/app.jar ${1:-"src/main/docker"}/app.jar || true
    docker build --pull -t ${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${CI_COMMIT_TAG} ${1:-"src/main/docker"}
    docker push ${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${CI_COMMIT_TAG}
}
function clean_cache(){
    rm -rf ${HOME}/.m2/${GROUP_NAME}/${CI_COMMIT_SHA}
}