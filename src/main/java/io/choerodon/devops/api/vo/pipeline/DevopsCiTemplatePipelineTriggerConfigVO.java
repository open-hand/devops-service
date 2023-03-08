package io.choerodon.devops.api.vo.pipeline;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsCiTemplatePipelineTriggerConfigVariableDTO;

public class DevopsCiTemplatePipelineTriggerConfigVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("触发的流水线id")
    private Long triggeredPipelineId;

    @ApiModelProperty("触发的其它流水线所属项目id")
    private Long triggeredPipelineProjectId;

    @ApiModelProperty("触发的分支")
    private String refName;

    private List<DevopsCiTemplatePipelineTriggerConfigVariableDTO> devopsCiPipelineVariables;

    public List<DevopsCiTemplatePipelineTriggerConfigVariableDTO> getDevopsCiPipelineVariables() {
        return devopsCiPipelineVariables;
    }

    public void setDevopsCiPipelineVariables(List<DevopsCiTemplatePipelineTriggerConfigVariableDTO> devopsCiPipelineVariables) {
        this.devopsCiPipelineVariables = devopsCiPipelineVariables;
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
}
