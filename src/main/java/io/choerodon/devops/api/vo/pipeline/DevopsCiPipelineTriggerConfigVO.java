package io.choerodon.devops.api.vo.pipeline;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class DevopsCiPipelineTriggerConfigVO {
    @Encrypt
    private Long id;

    @ApiModelProperty("流水线id")
    private Long pipelineId;

    @ApiModelProperty("触发的流水线id")
    @Encrypt
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

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getRefName() {
        return refName;
    }

    public void setRefName(String refName) {
        this.refName = refName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTriggerPipelineId() {
        return pipelineTriggerId;
    }

    public void setTriggerPipelineId(Long pipelineTriggerId) {
        this.pipelineTriggerId = pipelineTriggerId;
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
}
