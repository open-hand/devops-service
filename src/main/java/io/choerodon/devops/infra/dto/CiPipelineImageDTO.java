package io.choerodon.devops.infra.dto;

import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * @author scp
 * @date 2020/7/21
 * @description
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_ci_pipeline_image")
public class CiPipelineImageDTO extends AuditDomain {
    private Long id;
    private Long gitlabPipelineId;
    private String jobName;
    private String imageTag;
    private Long harborRepoId;
    private String repoType;

    public CiPipelineImageDTO() {
    }

    public Long getGitlabPipelineId() {
        return gitlabPipelineId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
