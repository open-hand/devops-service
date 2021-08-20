package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @Author: shanyu
 * @DateTime: 2021-08-19 18:49
 **/
public class DevopsDeployGroupVO {
    @Encrypt
    @ApiModelProperty(name = "id")
    private Long id;

    @ApiModelProperty(name = "name")
    private String name;

    @ApiModelProperty(name = "code")
    private String code;

    @Encrypt
    @ApiModelProperty(name = "项目id")
    private Long projectId;

    @Encrypt
    @ApiModelProperty(name = "Id")
    private Long envId;

    @ApiModelProperty(name = "应用配置")
    private String appConfig;

    @ApiModelProperty(name = "容器配置")
    private String containerConfig;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getAppConfig() {
        return appConfig;
    }

    public void setAppConfig(String appConfig) {
        this.appConfig = appConfig;
    }

    public String getContainerConfig() {
        return containerConfig;
    }

    public void setContainerConfig(String containerConfig) {
        this.containerConfig = containerConfig;
    }
}
