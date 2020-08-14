package io.choerodon.devops.infra.dto.harbor;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * description
 *
 * @author mofei.li@hand-china.com 2020/06/11 10:20
 */
@ApiModel("Harbor仓库配置DTO")
public class HarborRepoConfigDTO {
    @Encrypt
    @ApiModelProperty(value = "仓库ID")
    private Long repoId;

    @ApiModelProperty(value = "仓库地址")
    private String repoUrl;
    @ApiModelProperty(value = "仓库名称")
    private String repoName;
    @ApiModelProperty(value = "是否私有")
    private String isPrivate;

    @ApiModelProperty(value = "登录名")
    private String loginName;
    @ApiModelProperty(value = "密码")
    private String password;
    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "仓库的类型")
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getRepoId() {
        return repoId;
    }

    public void setRepoId(Long repoId) {
        this.repoId = repoId;
    }

    public String getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(String isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
