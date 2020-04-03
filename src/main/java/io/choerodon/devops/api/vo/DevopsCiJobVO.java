package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 *
 * @author wanghao
 * @Date 2020/4/2 17:00
 */
@Table(name = "devops_ci_job")
public class DevopsCiJobVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ApiModelProperty("任务名称")
    @NotEmpty(message = "error.job.name.cannot.be.null")
    private String name;
    @ApiModelProperty("阶段id")
    private Long stageId;
    @ApiModelProperty("流水线id")
    private Long ciPipelineId;
    @ApiModelProperty("任务类型")
    @NotEmpty(message = "error.job.type.cannot.be.null")
    private String type;
    @ApiModelProperty("触发分支")
    @NotEmpty(message = "error.job.triggerRefs.cannot.be.null")
    private String triggerRefs;
    @ApiModelProperty("详细信息")
    @NotEmpty(message = "error.job.metadata.cannot.be.null")
    private String metadata;

    private Long objectVersionNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public Long getCiPipelineId() {
        return ciPipelineId;
    }

    public void setCiPipelineId(Long ciPipelineId) {
        this.ciPipelineId = ciPipelineId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTriggerRefs() {
        return triggerRefs;
    }

    public void setTriggerRefs(String triggerRefs) {
        this.triggerRefs = triggerRefs;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
