package io.choerodon.devops.api.vo.cd;

import java.util.Date;
import java.util.List;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.dto.PipelineStageRecordDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * @author
 * @since 2022-11-25 14:59:00
 */
public class PipelineRecordVO extends AuditDomain {


    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "所属流水线Id,devops_pipeline.id", required = true)
    @NotNull
    private Long pipelineId;

    @ApiModelProperty(value = "状态", required = true)
    @NotBlank
    private String status;
    @ApiModelProperty(value = "流水线开始时间", required = true)
    private Date startedDate;
    @ApiModelProperty(value = "流水线结束时间", required = true)
    private Date finishedDate;
    @ApiModelProperty(value = "触发方式", required = true)
    private String triggerType;
    @ApiModelProperty(value = "触发应用服务id,devops_app_service.id", required = true)
    private Long appServiceId;
    @ApiModelProperty(value = "触发应用服务版本id,devops_app_service_version.id", required = true)
    private Long appServiceVersionId;

    private IamUserDTO trigger;

    private List<PipelineStageRecordDTO> stageRecordList;

    public List<PipelineStageRecordDTO> getStageRecordList() {
        return stageRecordList;
    }

    public void setStageRecordList(List<PipelineStageRecordDTO> stageRecordList) {
        this.stageRecordList = stageRecordList;
    }

    public IamUserDTO getTrigger() {
        return trigger;
    }

    public void setTrigger(IamUserDTO trigger) {
        this.trigger = trigger;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(Date startedDate) {
        this.startedDate = startedDate;
    }

    public Date getFinishedDate() {
        return finishedDate;
    }

    public void setFinishedDate(Date finishedDate) {
        this.finishedDate = finishedDate;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getAppServiceVersionId() {
        return appServiceVersionId;
    }

    public void setAppServiceVersionId(Long appServiceVersionId) {
        this.appServiceVersionId = appServiceVersionId;
    }
}
