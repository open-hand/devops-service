package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by Zenger on 2018/4/12.
 */
public class AppServiceInstanceVO {
    @Encrypt
    private Long id;
    @Encrypt
    @ApiModelProperty("所属应用服务id")
    private Long appServiceId;

    @ApiModelProperty("所属应用id")
    @Encrypt
    private Long appId;
    @Encrypt
    @ApiModelProperty("部署的环境id")
    private Long envId;
    @ApiModelProperty("关联的版本id")
    private Long appServiceVersionId;
    @ApiModelProperty("实例编码")
    private String code;
    @ApiModelProperty("关联的应用服务名称")
    private String appServiceName;
    @ApiModelProperty("关联的应用服务编码")
    private String appServiceCode;
    @ApiModelProperty("部署的版本号")
    private String appServiceVersion;
    @ApiModelProperty("部署的环境编码")
    private String envCode;
    @ApiModelProperty("部署的环境名称")
    private String envName;
    @ApiModelProperty("实例的状态")
    private String status;
    @Encrypt
    @ApiModelProperty("实例当前的commandId")
    private Long commandId;
    @ApiModelProperty("实例的总pod数")
    private Long podCount;
    @ApiModelProperty("实例运行中的pod数")
    private Long podRunningCount;
    @ApiModelProperty("实例的svc数")
    private Long serviceCount;
    @ApiModelProperty("实例的ingress数")
    private Long ingressCount;
    @ApiModelProperty("实例当前的command的状态")
    private String commandStatus;
    @ApiModelProperty("实例当前的command的类型，更新、新建")
    private String commandType;
    @ApiModelProperty("实例部署的版本")
    private String commandVersion;
    @ApiModelProperty("实例部署的版本id")
    private Long commandVersionId;
    @ApiModelProperty("错误信息")
    private String error;
    @ApiModelProperty("环境是否连接")
    private Boolean isConnect;
    @ApiModelProperty("乐观锁版本号")
    private Long objectVersionNumber;
    @ApiModelProperty("项目id")
    private Long projectId;

    @ApiModelProperty("应用实例的name")
    private String name;

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
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

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Long getAppServiceVersionId() {
        return appServiceVersionId;
    }

    public void setAppServiceVersionId(Long appServiceVersionId) {
        this.appServiceVersionId = appServiceVersionId;
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

    public Boolean getConnect() {
        return isConnect;
    }

    public void setConnect(Boolean connect) {
        isConnect = connect;
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


    public String getAppServiceCode() {
        return appServiceCode;
    }

    public void setAppServiceCode(String appServiceCode) {
        this.appServiceCode = appServiceCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
