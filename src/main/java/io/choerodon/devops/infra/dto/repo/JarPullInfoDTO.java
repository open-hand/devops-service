package io.choerodon.devops.infra.dto.repo;

import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 10:26
 */
public class JarPullInfoDTO {
    @ApiModelProperty("拉取账户")
    private String pullUserId;
    @ApiModelProperty("拉取密码")
    private String pullUserPassword;
    @ApiModelProperty("拉取地址")
    private String downloadUrl;

    public JarPullInfoDTO() {
    }

    public JarPullInfoDTO(String pullUserId, String pullUserPassword, String downloadUrl) {
        this.pullUserId = pullUserId;
        this.pullUserPassword = pullUserPassword;
        this.downloadUrl = downloadUrl;
    }

    public String getPullUserId() {
        return pullUserId;
    }

    public void setPullUserId(String pullUserId) {
        this.pullUserId = pullUserId;
    }

    public String getPullUserPassword() {
        return pullUserPassword;
    }

    public void setPullUserPassword(String pullUserPassword) {
        this.pullUserPassword = pullUserPassword;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
