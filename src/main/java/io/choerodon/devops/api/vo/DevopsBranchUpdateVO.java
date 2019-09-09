package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 */
public class DevopsBranchUpdateVO {
    @ApiModelProperty("应用服务id / 必填")
    @NotNull(message = "error.app.service.id.null")
    private Long appServiceId;

    @ApiModelProperty("关联的敏捷Issue的id / 必填")
    @NotNull(message = "error.branch.issue.id.null")
    private Long issueId;

    @ApiModelProperty("分支名 / 必填")
    @NotNull(message = "error.branch.name.null")
    private String branchName;

    @ApiModelProperty("分支纪录的版本号 / 必填")
    @NotNull(message = "error.object.version.number.null")
    private Long objectVersionNumber;

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
