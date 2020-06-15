package io.choerodon.devops.infra.dto.harbor;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * description
 *
 * @author mofei.li@hand-china.com 2020/06/11 10:20
 */
@ApiModel("Harbor仓库配置DTO")
public class HarborRepoConfigDTO {

    @ApiModelProperty(value = "仓库地址")
    private String repoUrl;
    @ApiModelProperty(value = "仓库名称")
    private String repoName;

    @ApiModelProperty(value = "登录名")
    private String loginName;
    @ApiModelProperty(value = "密码")
    private String password;

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
