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

function update_pom_version(){
    mvn versions:set -DnewVersion=${CI_COMMIT_TAG} || \
    find . -name pom.xml | xargs xml ed -L \
        -N x=http://maven.apache.org/POM/4.0.0 \
        -u '/x:project/x:version' -v "${CI_COMMIT_TAG}"
    mvn versions:commit
}