package io.choerodon.devops.api.vo;

import java.util.List;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zmf
 */
public class DevopsBranchUpdateVO {
    @ApiModelProperty("应用服务id / 必填")
    @NotNull(message = "error.app.service.id.null")
    @Encrypt
    private Long appServiceId;

    @ApiModelProperty("关联的敏捷Issue的ids")
    @Encrypt
    private List<Long> issueIds;

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

    public List<Long> getIssueIds() {
        return issueIds;
    }

    public void setIssueIds(List<Long> issueIds) {
        this.issueIds = issueIds;
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
