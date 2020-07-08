package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * @author wanghao
 * @since 2020/4/2 17:00
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_cd_job")
public class DevopsCdJobDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("任务名称")
    private String name;
    @ApiModelProperty("流水线id")
    private Long pipelineId;
    @ApiModelProperty("阶段id")
    private Long stageId;
    @ApiModelProperty("任务类型")
    private String type;
    /**
     * {@link io.choerodon.devops.infra.enums.CiTriggerType}
     */
    @ApiModelProperty("触发类型")
    private String triggerType;
    @ApiModelProperty("触发分支")
    private String triggerValue;
    @ApiModelProperty("镜像地址")
    private String metadata;
    @ApiModelProperty("项目ID")
    private Long projectId;
    @ApiModelProperty("是否会签")
    private Integer countersigned;
    @ApiModelProperty("任务顺序")
    private Long sequence;
    private Long deployInfoId;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }


    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getTriggerValue() {
        return triggerValue;
    }

    public void setTriggerValue(String triggerValue) {
        this.triggerValue = triggerValue;
    }

    public Integer getCountersigned() {
        return countersigned;
    }

    public void setCountersigned(Integer countersigned) {
        this.countersigned = countersigned;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getDeployInfoId() {
        return deployInfoId;
    }

    public void setDeployInfoId(Long deployInfoId) {
        this.deployInfoId = deployInfoId;
    }

    @Override
    public String toString() {
        return "DevopsCdJobDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", pipelineId=" + pipelineId +
                ", stageId=" + stageId +
                ", type='" + type + '\'' +
                ", triggerType='" + triggerType + '\'' +
                ", triggerValue='" + triggerValue + '\'' +
                ", metadata='" + metadata + '\'' +
                ", projectId=" + projectId +
                ", countersigned=" + countersigned +
                ", sequence=" + sequence +
                '}';
    }
}
