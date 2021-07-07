package io.choerodon.devops.infra.dto.repo;

import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/30 15:40
 */
public class DockerPullAccountDTO {
    @ApiModelProperty("url")
    private String harborUrl;

    @ApiModelProperty("拉取账号")
    private String pullAccount;

    @ApiModelProperty("拉取密码")
    private String pullPassword;

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
}
