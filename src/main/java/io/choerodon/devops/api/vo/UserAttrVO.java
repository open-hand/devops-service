package io.choerodon.devops.api.vo;


import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class UserAttrVO {
    @Encrypt
    @ApiModelProperty("iam用户id")
    private Long iamUserId;
    @ApiParam("gitlab用户id")
    private Long gitlabUserId;

    public UserAttrVO() {
    }

    public UserAttrVO(Long iamUserId, Long gitlabUserId) {
        this.iamUserId = iamUserId;
        this.gitlabUserId = gitlabUserId;
    }

    public Long getIamUserId() {
        return iamUserId;
    }

    public void setIamUserId(Long iamUserId) {
        this.iamUserId = iamUserId;
    }

    public Long getGitlabUserId() {
        return gitlabUserId;
    }

    public void setGitlabUserId(Long gitlabUserId) {
        this.gitlabUserId = gitlabUserId;
    }
}
