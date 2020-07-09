package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.annotation.WillDeleted;

/**
 *
 * @author wanghao
 * @Date 2020/4/2 17:00
 */
public class DevopsCiPipelineVO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ApiModelProperty("流水线名称")
    @NotEmpty(message = "error.pipeline.name.cannot.be.null")
    private String name;
    @ApiModelProperty("项目id")
    private Long projectId;
    @ApiModelProperty("流水线关联应用服务id")
    @NotNull(message = "error.pipeline.appSvc.id.cannot.be.null")
    private Long appServiceId;
    @ApiModelProperty("流水线关联应用服务名称/nullable")
    private String appServiceName;
    @ApiModelProperty("gitlab项目id/nullable")
    private Long gitlabProjectId;

    @ApiModelProperty("runner镜像地址")
    private String image;

    @ApiModelProperty("是否启用/nullable")
    private Boolean enabled;
    @ApiModelProperty("流水线触发方式")
    @NotEmpty(message = "error.pipeline.triggerType.cannot.be.null")
    private String triggerType;
    @ApiModelProperty("最近执行时间/nullable")
    private Date latestExecuteDate;
    @ApiModelProperty("最近执行记录状态/nullable")
    private String latestExecuteStatus;
    @ApiModelProperty("阶段信息")
    @Valid
    private List<DevopsCiStageVO> stageList;

    private Boolean hasMoreRecords;

    private List<DevopsCiPipelineRecordVO> pipelineRecordVOList;

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

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public List<DevopsCiStageVO> getStageList() {
        return stageList;
    }

    public void setStageList(List<DevopsCiStageVO> stageList) {
        this.stageList = stageList;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public List<DevopsCiPipelineRecordVO> getPipelineRecordVOList() {
        return pipelineRecordVOList;
    }

    public void setPipelineRecordVOList(List<DevopsCiPipelineRecordVO> pipelineRecordVOList) {
        this.pipelineRecordVOList = pipelineRecordVOList;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Date getLatestExecuteDate() {
        return latestExecuteDate;
    }

    public void setLatestExecuteDate(Date latestExecuteDate) {
        this.latestExecuteDate = latestExecuteDate;
    }

    public String getLatestExecuteStatus() {
        return latestExecuteStatus;
    }

    public void setLatestExecuteStatus(String latestExecuteStatus) {
        this.latestExecuteStatus = latestExecuteStatus;
    }

    public Boolean getHasMoreRecords() {
        return hasMoreRecords;
    }

    public void setHasMoreRecords(Boolean hasMoreRecords) {
        this.hasMoreRecords = hasMoreRecords;
    }

    public Long getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Long gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
