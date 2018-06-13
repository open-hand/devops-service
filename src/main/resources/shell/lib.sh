export GROUP_NAME={{ GROUP_NAME }}
export PROJECT_NAME={{ PROJECT_NAME }}
export CI_COMMIT_TAG=$(GetVersion)
function update_pom_version(){
    mvn versions:set -DnewVersion=${CI_COMMIT_TAG} || \
    find . -name pom.xml | xargs xml ed -L \
        -N x=http://maven.apache.org/POM/4.0.0 \
        -u '/x:project/x:version' -v "${CI_COMMIT_TAG}"
    mvn versions:commit
}