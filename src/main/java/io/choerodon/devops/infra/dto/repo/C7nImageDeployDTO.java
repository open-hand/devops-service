package io.choerodon.devops.infra.dto.repo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author scp
 * @date 2020/7/21
 * @description
 */
public class C7nImageDeployDTO {

    @ApiModelProperty("url")
    private String harborUrl;

    @ApiModelProperty("拉取账号")
    private String pullAccount;

    @ApiModelProperty("拉取密码")
    private String pullPassword;

    @ApiModelProperty("pull命令")
    private String pullCmd;

    public String getHarborUrl() {
        return harborUrl;
    }

    public void setHarborUrl(String harborUrl) {
        this.harborUrl = harborUrl;
    }

    public String getPullAccount() {
        return pullAccount;
    }

    public void setPullAccount(String pullAccount) {
        this.pullAccount = pullAccount;
    }

    public String getPullPassword() {
        return pullPassword;
    }

    public void setPullPassword(String pullPassword) {
        this.pullPassword = pullPassword;
    }

    public String getPullCmd() {
        return pullCmd;
    }

    public void setPullCmd(String pullCmd) {
        this.pullCmd = pullCmd;
    }
}
