package io.choerodon.devops.api.vo;

public class JobWebHookVO {

    private String sha;
    private String ref;
    private String buildName;
    private String buildStage;
    private String buildStatus;

    private JobCommitVO commit;

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    public String getBuildStage() {
        return buildStage;
    }

    public void setBuildStage(String buildStage) {
        this.buildStage = buildStage;
    }

    public String getBuildStatus() {
        return buildStatus;
    }

    public void setBuildStatus(String buildStatus) {
        this.buildStatus = buildStatus;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public JobCommitVO getCommit() {
        return commit;
    }

    public void setCommit(JobCommitVO commit) {
        this.commit = commit;
    }

    @Override
    public String toString() {
        return "JobWebHookVO{" +
                "sha='" + sha + '\'' +
                ", ref='" + ref + '\'' +
                ", buildName='" + buildName + '\'' +
                ", buildStage='" + buildStage + '\'' +
                ", buildStatus='" + buildStatus + '\'' +
                ", commit=" + commit +
                '}';
    }
}
