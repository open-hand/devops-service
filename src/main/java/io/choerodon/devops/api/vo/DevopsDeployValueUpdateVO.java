package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author lihao
 * @date 2019-09-15 15:48
 */
public class DevopsDeployValueUpdateVO {
    @ApiModelProperty("部署配置id")
    @NotNull(message = "error.deploy.value.id.null")
    private Long id;
    @ApiModelProperty("部署配置内容")
    @NotNull(message = "error.deploy.value.value.null")
    private String value;
    private Long projectId;
    @ApiModelProperty("环境id")
    @NotNull(message = "error.env.id.null")
    private Long envId;
    @ApiModelProperty("应用服务id")
    @NotNull(message = "error.app.service.id.null")
    private Long appServiceId;
    @ApiModelProperty("配置名称")
    @NotBlank(message = "error.deploy.value.name.null")
    private String name;
    @ApiModelProperty("部署配置描述")
    @NotBlank(message = "error.deploy.value.description.null")
    private String description;
    private String createUserUrl;
    private String createUserName;
    private String createUserRealName;
    @ApiModelProperty("环境状态")
    private Boolean envStatus;
    private Boolean index;
    @ApiModelProperty("环境名称")
    private String envName;
    @ApiModelProperty("服务名称")
    private String appServiceName;
    @ApiModelProperty("版本号/必须")
    private Long objectVersionNumber;

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public Boolean getIndex() {
        return index;
    }

    public void setIndex(Boolean index) {
        this.index = index;
    }

    public Boolean getEnvStatus() {
        return envStatus;
    }

    public void setEnvStatus(Boolean envStatus) {
        this.envStatus = envStatus;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
