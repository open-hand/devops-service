package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:27 2019/7/26
 * Description:
 */
public class AppServiceShareRuleVO {
    private Long id;

    @ApiModelProperty("应用服务Id")
    private Long appServiceId;

    @ApiModelProperty("共享层级,organization/project")
    private String shareLevel;

    @ApiModelProperty("共享版本类型")
    private String versionType;

    @ApiModelProperty("共享版本")
    private String version;
    @ApiModelProperty("共享到指定项目,项目Id")
    private Long projectId;

    @ApiModelProperty("共享到指定项目,项目名称")
    private String projectName;

    @ApiModelProperty("共享到指定应用,应用名称")
    private String appName;

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
}
