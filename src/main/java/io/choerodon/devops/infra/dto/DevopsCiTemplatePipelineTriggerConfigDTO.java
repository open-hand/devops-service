package io.choerodon.devops.infra.dto;

import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

@ModifyAudit
@VersionAudit
@Table(name = "devops_ci_tpl_pipeline_trigger_config")
public class DevopsCiTemplatePipelineTriggerConfigDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty("触发的流水线id")
    private Long triggeredPipelineId;

    @ApiModelProperty("触发的其它流水线所属项目id")
    private Long triggeredPipelineProjectId;


    @ApiModelProperty("触发的分支")
    private String refName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
