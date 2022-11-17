package io.choerodon.devops.infra.dto;

import javax.annotation.Nullable;
import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by Zenger on 2018/4/14.
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_app_service_instance")
public class AppServiceInstanceDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("实例的code")
    private String code;
    @ApiModelProperty("应用服务的id/当source为market时存市场服务的id")
    private Long appServiceId;
    @ApiModelProperty("实例部署的来源")
    private String source;
    /**
     * 这个应用服务版本是可能为空的，是集群中实际部署的实例的版本id
     * 假如第一次部署就失败了，这个值就可能是空的
     */
    @ApiModelProperty("应用版本id/如果是市场服务，就存的发布对象的id")
    @Nullable
    private Long appServiceVersionId;
    private Long envId;
    /**
     * 因为脏数据，还有可能为空
     */
    @Nullable
    private Long commandId;
    private String status;
    private Long valueId;
    @ApiModelProperty("组件所对应的实例的版本/普通实例这个值为null")
    private String componentVersion;
    @ApiModelProperty("组件对应实例的chart名称/普通实例这个值为null")
    private String componentChartName;
    @ApiModelProperty("当前实例生效的commandId")
    private Long effectCommandId;

    @ApiModelProperty("replicas生效策略在 values/deployment")
    private  String replicasStrategy;

    @Transient
    private String appServiceName;
    @Transient
    private String publishLevel;
    @Transient
    private String appServiceVersion;
    @Transient
    private String envCode;
    @Transient
    private String envName;
    @Transient
    private Long podCount;
    @Transient
    private Long podRunningCount;
    @Transient
    private Long serviceCount;
    @Transient
    private Long ingressCount;
    @Transient
    private String commandStatus;
    @Transient
    private String commandType;
    @Transient
    private String commandVersion;
    @Transient
    private Long commandVersionId;
    @Transient
    private String error;
    @Transient
    private Long projectId;
    @Transient
    private Integer isEnabled;
    @Transient
    private String appServiceCode;
    @Transient
    private String applicationType;

    public String getReplicasStrategy() {
        return replicasStrategy;
    }

    public void setReplicasStrategy(String replicasStrategy) {
        this.replicasStrategy = replicasStrategy;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public Long getEffectCommandId() {
        return effectCommandId;
    }

    public void setEffectCommandId(Long effectCommandId) {
        this.effectCommandId = effectCommandId;
    }

    public Long getAppServiceVersionId() {
        return appServiceVersionId;
    }

    public void setAppServiceVersionId(Long appServiceVersionId) {
        this.appServiceVersionId = appServiceVersionId;
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

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getEnvCode() {
        return envCode;
    }

    public void setEnvCode(String envCode) {
        this.envCode = envCode;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getPublishLevel() {
        return publishLevel;
    }

    public void setPublishLevel(String publishLevel) {
        this.publishLevel = publishLevel;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }


    public String getCommandVersion() {
        return commandVersion;
    }

    public void setCommandVersion(String commandVersion) {
        this.commandVersion = commandVersion;
    }

    public Long getCommandVersionId() {
        return commandVersionId;
    }

    public void setCommandVersionId(Long commandVersionId) {
        this.commandVersionId = commandVersionId;
    }

    public Integer getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Integer isEnabled) {
        this.isEnabled = isEnabled;
    }

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

    public Long getServiceCount() {
        return serviceCount;
    }

    public void setServiceCount(Long serviceCount) {
        this.serviceCount = serviceCount;
    }

    public Long getIngressCount() {
        return ingressCount;
    }

    public void setIngressCount(Long ingressCount) {
        this.ingressCount = ingressCount;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public String getAppServiceVersion() {
        return appServiceVersion;
    }

    public void setAppServiceVersion(String appServiceVersion) {
        this.appServiceVersion = appServiceVersion;
    }

    public String getAppServiceCode() {
        return appServiceCode;
    }

    public void setAppServiceCode(String appServiceCode) {
        this.appServiceCode = appServiceCode;
    }

    public String getComponentVersion() {
        return componentVersion;
    }

    public void setComponentVersion(String componentVersion) {
        this.componentVersion = componentVersion;
    }

    public String getComponentChartName() {
        return componentChartName;
    }

    public void setComponentChartName(String componentChartName) {
        this.componentChartName = componentChartName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
