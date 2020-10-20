package io.choerodon.devops.api.vo;

import java.util.Date;

import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/10/12 15:14
 */
public class DeployRecordVO {
    @Encrypt
    private Long id;

    private String viewId;

    private String deployType;

    private String deployResult;

    @Encrypt
    private Long instanceId;

    private String instanceName;

    private String deployMode;

    @Encrypt
    private Long deployPayloadId;

    private String deployPayloadName;

    private Date deployTime;

    private String deployObjectType;

    private String deployObjectName;

    private String deployObjectVersion;

    private Long clusterId;

    private Boolean connect;

    @Encrypt
    private Long appServiceId;

    private String appServiceName;

    private String appServiceVersion;

    private IamUserDTO executeUser;

    private Long createdBy;

    private String commandStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public String getDeployType() {
        return deployType;
    }

    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    public String getDeployResult() {
        return deployResult;
    }

    public void setDeployResult(String deployResult) {
        this.deployResult = deployResult;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
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

    public IamUserDTO getExecuteUser() {
        return executeUser;
    }

    public void setExecuteUser(IamUserDTO executeUser) {
        this.executeUser = executeUser;
    }

    public Date getDeployTime() {
        return deployTime;
    }

    public void setDeployTime(Date deployTime) {
        this.deployTime = deployTime;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public Boolean getConnect() {
        return connect;
    }

    public void setConnect(Boolean connect) {
        this.connect = connect;
    }

    public String getDeployMode() {
        return deployMode;
    }

    public void setDeployMode(String deployMode) {
        this.deployMode = deployMode;
    }

    public Long getDeployPayloadId() {
        return deployPayloadId;
    }

    public void setDeployPayloadId(Long deployPayloadId) {
        this.deployPayloadId = deployPayloadId;
    }

    public String getDeployPayloadName() {
        return deployPayloadName;
    }

    public void setDeployPayloadName(String deployPayloadName) {
        this.deployPayloadName = deployPayloadName;
    }

    public String getDeployObjectType() {
        return deployObjectType;
    }

    public void setDeployObjectType(String deployObjectType) {
        this.deployObjectType = deployObjectType;
    }

    public String getDeployObjectName() {
        return deployObjectName;
    }

    public void setDeployObjectName(String deployObjectName) {
        this.deployObjectName = deployObjectName;
    }

    public String getDeployObjectVersion() {
        return deployObjectVersion;
    }

    public void setDeployObjectVersion(String deployObjectVersion) {
        this.deployObjectVersion = deployObjectVersion;
    }

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }
}
