package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.pipeline.ExternalApprovalJobVO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * @author wanghao
 * @since 2020/4/2 17:00
 */
public class DevopsCdJobVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("任务名称")
    @NotEmpty(message = "error.job.name.cannot.be.null")
    private String name;

    @Encrypt
    @ApiModelProperty("阶段id")
    private Long stageId;

    @Encrypt
    @ApiModelProperty("流水线id")
    private Long pipelineId;
    @ApiModelProperty("任务类型")

    /**
     * {@link io.choerodon.devops.infra.enums.JobTypeEnum}
     */
    @NotEmpty(message = "error.job.type.cannot.be.null")
    private String type;
    @ApiModelProperty("触发类型对应的值")
    private String triggerValue;

    /**
     * {@link io.choerodon.devops.infra.enums.CiTriggerType}
     */
    @ApiModelProperty("触发类型")
    private String triggerType;

    @ApiModelProperty("详细信息")
    private String metadata;
    private String envName;
    @NotNull(message = "error.job.sequence.cannot.be.null")
    private Long sequence;

    @Encrypt
    private Long deployInfoId;

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    //审核人员的集合
    private List<IamUserDTO> iamUserDTOS;
    @Encrypt
    private List<Long> cdAuditUserIds;
    //是否会签
    private Integer countersigned;
    private Long projectId;
    private Date lastUpdateDate;
    private Long objectVersionNumber;

    private ExternalApprovalJobVO externalApprovalJobVO;

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public List<IamUserDTO> getIamUserDTOS() {
        return iamUserDTOS;
    }

    public void setIamUserDTOS(List<IamUserDTO> iamUserDTOS) {
        this.iamUserDTOS = iamUserDTOS;
    }

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

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTriggerValue() {
        return triggerValue;
    }

    public void setTriggerValue(String triggerValue) {
        this.triggerValue = triggerValue;
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


    public List<Long> getCdAuditUserIds() {
        return cdAuditUserIds;
    }

    public void setCdAuditUserIds(List<Long> cdAuditUserIds) {
        this.cdAuditUserIds = cdAuditUserIds;
    }

    public Integer getCountersigned() {
        return countersigned;
    }

    public void setCountersigned(Integer countersigned) {
        this.countersigned = countersigned;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Long getDeployInfoId() {
        return deployInfoId;
    }

    public void setDeployInfoId(Long deployInfoId) {
        this.deployInfoId = deployInfoId;
    }

    public ExternalApprovalJobVO getExternalApprovalJobVO() {
        return externalApprovalJobVO;
    }

    public void setExternalApprovalJobVO(ExternalApprovalJobVO externalApprovalJobVO) {
        this.externalApprovalJobVO = externalApprovalJobVO;
    }
}
