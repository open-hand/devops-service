package io.choerodon.devops.api.vo;

import java.util.Date;

public class JobWebHookVO {

    private String sha;
    private String ref;
    private Long buildId;
    private String buildName;
    private String buildStage;
    private String buildStatus;
    private Date buildStartedAt;
    private Date buildFinishedAt;
    private Long buildDuration;
    private Long pipelineId;

    private JobCommitVO commit;

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

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

    public Long getBuildId() {
        return buildId;
    }

    public void setBuildId(Long buildId) {
        this.buildId = buildId;
    }

    public Date getBuildStartedAt() {
        return buildStartedAt;
    }

    public void setBuildStartedAt(Date buildStartedAt) {
        this.buildStartedAt = buildStartedAt;
    }

    public Date getBuildFinishedAt() {
        return buildFinishedAt;
    }

    public void setBuildFinishedAt(Date buildFinishedAt) {
        this.buildFinishedAt = buildFinishedAt;
    }

    public Long getBuildDuration() {
        return buildDuration;
    }

    public void setBuildDuration(Long buildDuration) {
        this.buildDuration = buildDuration;
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
