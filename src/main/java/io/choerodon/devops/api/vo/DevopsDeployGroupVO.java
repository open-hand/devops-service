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
    @Encrypt
    @ApiModelProperty(name = "id,也就是deployment的id")
    private Long id;

    @ApiModelProperty("应用中心应用名称")
    @Size(min = 1, max = 64, message = "error.env.app.center.name.length")
    @NotBlank(message = "error.app.instance.name.null")
    private String appName;

    @ApiModelProperty("应用中心应用code，同时也作为实例名称")
    @Size(min = 1, max = 64, message = "error.env.app.center.code.length")
    @NotBlank(message = "error.app.instance.code.null")
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
    @ApiModelProperty(name = "关联的应用中心的id")
    private Long instanceId;

    private Long objectVersionNumber;

    private Long instanceObjectVersionNumber;

    @JsonIgnore
    private String appConfigJson;
    @JsonIgnore
    private String containerConfigJson;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
