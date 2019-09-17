package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.api.validator.annotation.AtLeastOneNotEmpty;

/**
 * @author lihao
 * @date 2019-09-15 15:32
 */
@AtLeastOneNotEmpty(fields = {"versionType", "version"})
public class AppServiceShareRuleUpdateVO {
    @ApiModelProperty("共享规则id")
    @NotNull(message = "error.app.share.id.null")
    private Long id;

    @ApiModelProperty("应用服务Id/必填")
    @NotNull(message = "error.app.id.null")
    private Long appServiceId;

    @ApiModelProperty("共享层级,organization/project 必填")
    @NotBlank(message = "error.app.share.level.null")
    private String shareLevel;

    @ApiModelProperty("共享版本类型")
    private String versionType;

    @ApiModelProperty("共享版本")
    private String version;

    @ApiModelProperty("共享到指定项目,项目Id/必填")
    @NotNull(message = "error.app.share.project.null")
    private Long projectId;

    @ApiModelProperty("共享到指定项目,项目名称")
    @NotBlank(message = "error.app.share.projectName.null")
    private String projectName;

    @ApiModelProperty("共享到指定应用,应用名称")
    private String appName;

    @ApiModelProperty("版本号/必须")
    @NotNull(message = "error.object.version.number.null")
    private Long objectVersionNumber;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getShareLevel() {
        return shareLevel;
    }

    public void setShareLevel(String shareLevel) {
        this.shareLevel = shareLevel;
    }

    public String getVersionType() {
        return versionType;
    }

    public void setVersionType(String versionType) {
        this.versionType = versionType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
