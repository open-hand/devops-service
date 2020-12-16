package io.choerodon.devops.api.vo.market;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

import io.choerodon.devops.api.vo.HarborC7nImageTagVo;

/**
 * 应用市场harbor的仓库配置
 *
 * @author zmf
 * @since 2020/12/1
 */
public class MarketHarborConfigVO {
    @ApiModelProperty("harbor仓库地址")
    private String repoUrl;

    @ApiModelProperty("这个harbor仓库的owner用户名")
    private String ownerUserName;

    @ApiModelProperty("这个harbor仓库的owner密码")
    private String ownerPassword;

    @ApiModelProperty("拉取镜像的机器人用户名字")
    private String robotName;

    @ApiModelProperty("拉取镜像的机器人用户token")
    private String token;

    public String getToken() {
        return token;
    }

    /**
     * 镜像列表
     */
    private List<HarborC7nImageTagVo> harborC7nImageTagVoList;

    public List<HarborC7nImageTagVo> getHarborC7nImageTagVoList() {
        return harborC7nImageTagVoList;
    }

    public void setHarborC7nImageTagVoList(List<HarborC7nImageTagVo> harborC7nImageTagVoList) {
        this.harborC7nImageTagVoList = harborC7nImageTagVoList;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRobotName() {
        return robotName;
    }

    public void setRobotName(String robotName) {
        this.robotName = robotName;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getOwnerUserName() {
        return ownerUserName;
    }

    public void setOwnerUserName(String ownerUserName) {
        this.ownerUserName = ownerUserName;
    }

    public String getOwnerPassword() {
        return ownerPassword;
    }

    public void setOwnerPassword(String ownerPassword) {
        this.ownerPassword = ownerPassword;
    }
}
