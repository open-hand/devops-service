package io.choerodon.devops.api.vo.market;

import io.swagger.annotations.ApiModelProperty;

/**
 * 应用市场的chart仓库配置
 *
 * @author zmf
 * @since 2020/12/1
 */
public class MarketChartConfigVO {
    @ApiModelProperty("helm仓库地址")
    private String repoUrl;
    @ApiModelProperty("用户名/可为空")
    private String username;
    @ApiModelProperty("密码/可为空")
    private String password;

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
