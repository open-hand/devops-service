package io.choerodon.devops.infra.dto;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;

/**
 * 展示实例详情页面的单个实例的信息
 *
 * @author zmf
 */
public class AppServiceInstanceInfoDTO {
    private Long id;
    private String code;
    private String status;
    private Long podCount;
    private Long podRunningCount;
    private Long appServiceId;
    private Long appServiceVersionId;
    private String appServiceName;
    private String versionName;
    private Date lastUpdateDate;
    private Long objectVersionNumber;
    private Long environmentId;
    private Long clusterId;
    private Long commandVersionId;
    private String commandVersion;
    private String commandType;
    private String commandStatus;
    private String error;
    private Long projectId;
    @ApiModelProperty("当前实例生效的commandId/可能为null")
    private Long effectCommandId;
    @ApiModelProperty("当前实例生效的版本/可能为null")
    private String effectCommandVersion;
    @ApiModelProperty("当前实例生效的command的状态/可能为null，为")
    private String effectCommandStatus;
    @ApiModelProperty("当前实例生效的版本id/可能为null")
    private Long effectCommandVersionId;
    @ApiModelProperty("实例来源")
    private String source;
    @ApiModelProperty("应用名称")
    private String name;
    @ApiModelProperty("更新者")
    private Long lastUpdatedBy;

    @ApiModelProperty("是否开启确认副本生效策略，默认为false")
    private Boolean checkValuesPolicy;

    public Boolean getCheckValuesPolicy() {
        return checkValuesPolicy;
    }

    public void setCheckValuesPolicy(Boolean checkValuesPolicy) {
        this.checkValuesPolicy = checkValuesPolicy;
    }

    public Long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(Long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getPodCount() {
        return podCount;
    }

    public void setPodCount(Long podCount) {
        this.podCount = podCount;
    }

    public Long getPodRunningCount() {
        return podRunningCount;
    }

    public void setPodRunningCount(Long podRunningCount) {
        this.podRunningCount = podRunningCount;
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

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
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

    public Long getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Long environmentId) {
        this.environmentId = environmentId;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public Long getCommandVersionId() {
        return commandVersionId;
    }

    public void setCommandVersionId(Long commandVersionId) {
        this.commandVersionId = commandVersionId;
    }

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public String getCommandVersion() {
        return commandVersion;
    }

    public void setCommandVersion(String commandVersion) {
        this.commandVersion = commandVersion;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getEffectCommandId() {
        return effectCommandId;
    }

    public void setEffectCommandId(Long effectCommandId) {
        this.effectCommandId = effectCommandId;
    }

    public String getEffectCommandVersion() {
        return effectCommandVersion;
    }

    public void setEffectCommandVersion(String effectCommandVersion) {
        this.effectCommandVersion = effectCommandVersion;
    }

    public String getEffectCommandStatus() {
        return effectCommandStatus;
    }

    public void setEffectCommandStatus(String effectCommandStatus) {
        this.effectCommandStatus = effectCommandStatus;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Long getEffectCommandVersionId() {
        return effectCommandVersionId;
    }

    public void setEffectCommandVersionId(Long effectCommandVersionId) {
        this.effectCommandVersionId = effectCommandVersionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
