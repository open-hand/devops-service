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
if [ $CI_COMMIT_TAG ]; then
    export C7N_GITVERSION=$CI_COMMIT_TAG
elif [ $CIRCLE_TAG ]; then
    export C7N_GITVERSION=$CIRCLE_TAG
else
    export C7N_GITVERSION=$C7N_GIT_COMMIT_TIME-$C7N_GIT_BRANCH
fi

export CI_COMMIT_TAG=$C7N_GITVERSION

function update_pom_version(){
    mvn versions:set -DnewVersion=${CI_COMMIT_TAG} || \
    find . -name pom.xml | xargs xml ed -L \
        -N x=http://maven.apache.org/POM/4.0.0 \
        -u '/x:project/x:version' -v "${CI_COMMIT_TAG}"
    mvn versions:commit
}