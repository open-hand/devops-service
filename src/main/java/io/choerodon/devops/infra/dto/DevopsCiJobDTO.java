package io.choerodon.devops.infra.dto;

import io.choerodon.mybatis.entity.BaseDTO;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author wanghao
 * @Date 2020/4/2 17:00
 */
@Table(name = "devops_ci_job")
public class DevopsCiJobDTO extends BaseDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ApiModelProperty("任务名称")
    private String name;
    @ApiModelProperty("阶段id")
    private Long ciStageId;
    @ApiModelProperty("流水线id")
    private Long ciPipelineId;
    @ApiModelProperty("任务类型")
    private String type;
    @ApiModelProperty("触发分支")
    private String triggerRefs;
    @ApiModelProperty("详细信息")
    private String metadata;

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

    public Long getCiStageId() {
        return ciStageId;
    }

    public void setCiStageId(Long ciStageId) {
        this.ciStageId = ciStageId;
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
}
