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

    @ApiModelProperty("集群中拉取镜像时的secretName")
    private String secretCode;

    public DockerPullAccountDTO() {
    }

    public DockerPullAccountDTO(String harborUrl, String pullAccount, String pullPassword) {
        this.harborUrl = harborUrl;
        this.pullAccount = pullAccount;
        this.pullPassword = pullPassword;
    }

    public String getHarborUrl() {
        return harborUrl;
    }

    public DockerPullAccountDTO setHarborUrl(String harborUrl) {
        this.harborUrl = harborUrl;
        return this;
    }

    public String getPullAccount() {
        return pullAccount;
    }

    public DockerPullAccountDTO setPullAccount(String pullAccount) {
        this.pullAccount = pullAccount;
        return this;
    }

    public String getPullPassword() {
        return pullPassword;
    }

    public DockerPullAccountDTO setPullPassword(String pullPassword) {
        this.pullPassword = pullPassword;
        return this;
    }

    public String getSecretCode() {
        return secretCode;
    }

    public DockerPullAccountDTO setSecretCode(String secretCode) {
        this.secretCode = secretCode;
        return this;
    }
}
