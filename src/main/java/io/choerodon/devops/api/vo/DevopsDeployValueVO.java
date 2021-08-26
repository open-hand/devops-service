package io.choerodon.devops.api.vo;


import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:44 2019/4/10
 * Description:
 */
public class DevopsDeployValueVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("部署配置内容")
    @NotNull(message = "error.deploy.value.value.null")
    private String value;

    private Long projectId;

    @Encrypt
    @ApiModelProperty("环境id")
    @NotNull(message = "error.env.id.null")
    private Long envId;

    @Encrypt
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
    private Long createdBy;
    private Boolean envStatus;
    private Date lastUpdateDate;
    private Boolean index;
    private String envName;
    @ApiModelProperty("服务名称")
    private String appServiceName;

    private Long objectVersionNumber;

    private IamUserDTO creator;

    public IamUserDTO getCreator() {
        return creator;
    }

    public void setCreator(IamUserDTO creator) {
        this.creator = creator;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
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
