package io.choerodon.devops.api.vo;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @Author: shanyu
 * @DateTime: 2021-08-19 18:49
 **/
public class DevopsDeployGroupVO {
    @ApiModelProperty("应用中心应用名称")
    private String appName;

    @ApiModelProperty("应用中心应用code，同时也作为实例名称")
    private String appCode;

    @Encrypt
    @ApiModelProperty(name = "项目id")
    private Long projectId;

    @Encrypt
    @ApiModelProperty(name = "Id")
    private Long envId;

    @ApiModelProperty(name = "应用配置")
    private DevopsDeployGroupAppConfigVO appConfig;

    @ApiModelProperty(name = "容器配置")
    private List<DevopsDeployGroupContainerConfigVO> containerConfig;

    @Encrypt
    @ApiModelProperty(name = "关联的deploymentId")
    private Long instanceId;

    private Long objectVersionNumber;

    private Long instanceObjectVersionNumber;

    @JsonIgnore
    private String appConfigJson;
    @JsonIgnore
    private String containerConfigJson;

    public DevopsDeployGroupVO() {
    }

    public DevopsDeployGroupVO(String appName,
                               String appCode,
                               Long projectId,
                               Long envId,
                               DevopsDeployGroupAppConfigVO appConfig,
                               List<DevopsDeployGroupContainerConfigVO> containerConfig,
                               Long instanceId) {
        this.appName = appName;
        this.appCode = appCode;
        this.projectId = projectId;
        this.envId = envId;
        this.appConfig = appConfig;
        this.containerConfig = containerConfig;
        this.instanceId = instanceId;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppName() {
        return this.appName;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getAppCode() {
        return this.appCode;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public DevopsDeployGroupAppConfigVO getAppConfig() {
        return appConfig;
    }

    public void setAppConfig(DevopsDeployGroupAppConfigVO appConfig) {
        this.appConfig = appConfig;
    }

    public List<DevopsDeployGroupContainerConfigVO> getContainerConfig() {
        return containerConfig;
    }

    public void setContainerConfig(List<DevopsDeployGroupContainerConfigVO> containerConfig) {
        this.containerConfig = containerConfig;
    }

    public String getAppConfigJson() {
        return appConfigJson;
    }

    public void setAppConfigJson(String appConfigJson) {
        this.appConfigJson = appConfigJson;
    }

    public String getContainerConfigJson() {
        return containerConfigJson;
    }

    public void setContainerConfigJson(String containerConfigJson) {
        this.containerConfigJson = containerConfigJson;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Long getInstanceObjectVersionNumber() {
        return instanceObjectVersionNumber;
    }

    public void setInstanceObjectVersionNumber(Long instanceObjectVersionNumber) {
        this.instanceObjectVersionNumber = instanceObjectVersionNumber;
    }
}
