package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.dto.PipelineStageRecordDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * @author
 * @since 2022-11-24 16:08:35
 */
public class PipelineHomeVO {

    private Long id;
    @ApiModelProperty(value = "项目id", required = true)
    private Long projectId;
    @ApiModelProperty(value = "流水线名称", required = true)
    private String name;
    @ApiModelProperty(value = "令牌", required = false)
    private String token;
    @ApiModelProperty(value = "是否启用", required = false)
    private Boolean enable;
    @ApiModelProperty(value = "是否开启应用服务版本生成触发", required = false)
    private Boolean appVersionTriggerEnable;
    @ApiModelProperty(value = "状态", required = true)
    @NotBlank
    private String status;
    @ApiModelProperty(value = "流水线开始时间", required = true)
    private Date startedDate;
    @ApiModelProperty(value = "流水线结束时间", required = true)
    private Date finishedDate;
    @ApiModelProperty(value = "触发方式", required = true)
    private String triggerType;

    @ApiModelProperty(hidden = true)
    private Long createdBy;

    private Long latestPipelineRecordId;

    private Long latestPipelineTriggerId;


    @ApiModelProperty("触发者")
    private IamUserDTO trigger;

    public Long getLatestPipelineTriggerId() {
        return latestPipelineTriggerId;
    }

    public void setLatestPipelineTriggerId(Long latestPipelineTriggerId) {
        this.latestPipelineTriggerId = latestPipelineTriggerId;
    }

    public Long getLatestPipelineRecordId() {
        return latestPipelineRecordId;
    }

    public void setLatestPipelineRecordId(Long latestPipelineRecordId) {
        this.latestPipelineRecordId = latestPipelineRecordId;
    }

    private List<PipelineStageRecordDTO> stageRecordList;

    public List<PipelineStageRecordDTO> getStageRecordList() {
        return stageRecordList;
    }

    public void setStageRecordList(List<PipelineStageRecordDTO> stageRecordList) {
        this.stageRecordList = stageRecordList;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Boolean getAppVersionTriggerEnable() {
        return appVersionTriggerEnable;
    }

    public void setAppVersionTriggerEnable(Boolean appVersionTriggerEnable) {
        this.appVersionTriggerEnable = appVersionTriggerEnable;
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

    public IamUserDTO getTrigger() {
        return trigger;
    }

    public void setTrigger(IamUserDTO trigger) {
        this.trigger = trigger;
    }
}
