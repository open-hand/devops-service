package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotNull;

/**
 * @author scp
 * @date 2020/7/21
 * @description
 */
public class CiPipelineImageVO {
    @NotNull
    private Long gitlabPipelineId;
    @NotNull
    private String jobName;
    @NotNull
    private String imageTag;
    @NotNull
    private Long harborRepoId;
    @NotNull
    private String repoType;

    public Long getGitlabPipelineId() {
        return gitlabPipelineId;
    }

    public void setGitlabPipelineId(Long gitlabPipelineId) {
        this.gitlabPipelineId = gitlabPipelineId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getImageTag() {
        return imageTag;
    }

    public void setImageTag(String imageTag) {
        this.imageTag = imageTag;
    }

    public Long getHarborRepoId() {
        return harborRepoId;
    }

    public void setHarborRepoId(Long harborRepoId) {
        this.harborRepoId = harborRepoId;
    }

    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }
}
