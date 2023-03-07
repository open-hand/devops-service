package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

@ModifyAudit
@VersionAudit
@Table(name = "devops_ci_pipeline_trigger_config")
public class DevopsCiPipelineTriggerConfigDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty("流水线job id")
    private Long jobId;

    @ApiModelProperty("触发的流水线id")
    private Long triggeredPipelineId;

    @ApiModelProperty("触发的其它流水线所属项目id")
    private Long triggeredPipelineProjectId;

    @ApiModelProperty("触发的其它流水线gitlab 项目id")
    private Long triggeredPipelineGitlabProjectId;

    @ApiModelProperty("流水线trigger id")
    private Long pipelineTriggerId;

    @ApiModelProperty("触发的分支")
    private String refName;

    @ApiModelProperty("token")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getTriggeredPipelineId() {
        return triggeredPipelineId;
    }

    public void setTriggeredPipelineId(Long triggeredPipelineId) {
        this.triggeredPipelineId = triggeredPipelineId;
    }

    public Long getTriggeredPipelineProjectId() {
        return triggeredPipelineProjectId;
    }

    public void setTriggeredPipelineProjectId(Long triggeredPipelineProjectId) {
        this.triggeredPipelineProjectId = triggeredPipelineProjectId;
    }

    public Long getTriggeredPipelineGitlabProjectId() {
        return triggeredPipelineGitlabProjectId;
    }

    public void setTriggeredPipelineGitlabProjectId(Long triggeredPipelineGitlabProjectId) {
        this.triggeredPipelineGitlabProjectId = triggeredPipelineGitlabProjectId;
    }

    public Long getPipelineTriggerId() {
        return pipelineTriggerId;
    }

    public void setPipelineTriggerId(Long pipelineTriggerId) {
        this.pipelineTriggerId = pipelineTriggerId;
    }

    public String getRefName() {
        return refName;
    }

    public void setRefName(String refName) {
        this.refName = refName;
    }
}
