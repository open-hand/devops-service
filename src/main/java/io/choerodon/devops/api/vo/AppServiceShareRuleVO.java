package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.validator.annotation.AtLeastOneNotEmpty;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:27 2019/7/26
 * Description:
 */
@AtLeastOneNotEmpty(fields = {"versionType", "version", "appServiceId"}, message = "{devops.atleast.one.not.empty}")
public class AppServiceShareRuleVO {
    @Encrypt
    private Long id;

    @Encrypt
    @ApiModelProperty("应用服务Id/必填")
    @NotNull(message = "{devops.app.id.null}")
    private Long appServiceId;

    @ApiModelProperty("共享层级,organization/project 必填")
    @NotBlank(message = "{devops.app.share.level.null}")
    private String shareLevel;

    @ApiModelProperty("共享版本类型")
    private String versionType;

    @ApiModelProperty("共享版本")
    private String version;

    @ApiModelProperty("共享到指定项目,项目Id/必填")
    private Long projectId;

    @ApiModelProperty("共享到指定项目,项目名称")
    private String projectName;
    @ApiModelProperty("乐观锁版本号")
    private Long objectVersionNumber;
    @ApiModelProperty("界面展示id")
    private String viewId;

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

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

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
