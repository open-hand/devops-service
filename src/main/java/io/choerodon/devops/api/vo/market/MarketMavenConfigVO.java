package io.choerodon.devops.api.vo.market;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 应用市场maven的配置
 *
 * @author zmf
 * @since 2020/12/1
 */
public class MarketMavenConfigVO {
    @ApiModelProperty("maven的仓库地址")
    private String repoUrl;
    @ApiModelProperty("有推jar包权限的用户名")
    private String pushUserName;
    @ApiModelProperty("有推jar包权限的密码")
    private String pushPassword;
    @ApiModelProperty("有拉取jar包权限的用户名")
    private String pullUserName;
    @ApiModelProperty("有拉取jar包权限的密码")
    private String pullPassword;

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getPushUserName() {
        return pushUserName;
    }

    public void setPushUserName(String pushUserName) {
        this.pushUserName = pushUserName;
    }

    public String getPushPassword() {
        return pushPassword;
    }

    public void setPushPassword(String pushPassword) {
        this.pushPassword = pushPassword;
    }

    public String getPullUserName() {
        return pullUserName;
    }

    public void setPullUserName(String pullUserName) {
        this.pullUserName = pullUserName;
    }

    public String getPullPassword() {
        return pullPassword;
    }

    public void setPullPassword(String pullPassword) {
        this.pullPassword = pullPassword;
    }
}
