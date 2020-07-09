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
import org.hzero.starter.keyencrypt.core.Encrypt;

public class CiCdPipelineVO {
    @Encrypt
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty("流水线名称")
    @NotEmpty(message = "error.pipeline.name.cannot.be.null")
    private String name;

    @Encrypt
    @ApiModelProperty("项目id")
    private Long projectId;

    @Encrypt
    @ApiModelProperty("流水线关联应用服务id")
    @NotNull(message = "error.pipeline.appSvc.id.cannot.be.null")
    private Long appServiceId;

    @ApiModelProperty("流水线关联应用服务名称/nullable")
    private String appServiceName;
    private String appServiceType;
    private String appServiceCode;
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
    @ApiModelProperty(name = "ci阶段信息")
    @Valid
    private List<DevopsCiStageVO> devopsCiStageVOS;

    @ApiModelProperty(name = "cd阶段信息")
    @Valid
    private List<DevopsCdStageVO> devopsCdStageVOS;

    private Boolean hasMoreRecords;
    //cicd 流水线下的执行记录
    private List<CiCdPipelineRecordVO> ciCdPipelineRecordVOS;


    private Long objectVersionNumber;

    private Boolean edit;
    private List<Long> pipelineUserRels;
    private Date lastUpdateDate;
    private String createUserUrl;
    private String createUserName;
    private String createUserRealName;
    private Long createdBy;
    private Boolean execute;
    private String envName;
    //流程耗时
    private Long time;

    public String getAppServiceType() {
        return appServiceType;
    }

    public void setAppServiceType(String appServiceType) {
        this.appServiceType = appServiceType;
    }

    public List<CiCdPipelineRecordVO> getCiCdPipelineRecordVOS() {
        return ciCdPipelineRecordVOS;
    }

    public void setCiCdPipelineRecordVOS(List<CiCdPipelineRecordVO> ciCdPipelineRecordVOS) {
        this.ciCdPipelineRecordVOS = ciCdPipelineRecordVOS;
    }


    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public String getAppServiceCode() {
        return appServiceCode;
    }

    public void setAppServiceCode(String appServiceCode) {
        this.appServiceCode = appServiceCode;
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
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

    public List<DevopsCiStageVO> getDevopsCiStageVOS() {
        return devopsCiStageVOS;
    }

    public void setDevopsCiStageVOS(List<DevopsCiStageVO> devopsCiStageVOS) {
        this.devopsCiStageVOS = devopsCiStageVOS;
    }

    public List<DevopsCdStageVO> getDevopsCdStageVOS() {
        return devopsCdStageVOS;
    }

    public void setDevopsCdStageVOS(List<DevopsCdStageVO> devopsCdStageVOS) {
        this.devopsCdStageVOS = devopsCdStageVOS;
    }

    public Boolean getHasMoreRecords() {
        return hasMoreRecords;
    }

    public void setHasMoreRecords(Boolean hasMoreRecords) {
        this.hasMoreRecords = hasMoreRecords;
    }


    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Boolean getEdit() {
        return edit;
    }

    public void setEdit(Boolean edit) {
        this.edit = edit;
    }

    public List<Long> getPipelineUserRels() {
        return pipelineUserRels;
    }

    public void setPipelineUserRels(List<Long> pipelineUserRels) {
        this.pipelineUserRels = pipelineUserRels;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getCreateUserUrl() {
        return createUserUrl;
    }

    public void setCreateUserUrl(String createUserUrl) {
        this.createUserUrl = createUserUrl;
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    public String getCreateUserRealName() {
        return createUserRealName;
    }

    public void setCreateUserRealName(String createUserRealName) {
        this.createUserRealName = createUserRealName;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Boolean getExecute() {
        return execute;
    }

    public void setExecute(Boolean execute) {
        this.execute = execute;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

}
