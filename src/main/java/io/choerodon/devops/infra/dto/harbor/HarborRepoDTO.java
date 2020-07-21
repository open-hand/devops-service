package io.choerodon.devops.infra.dto.harbor;


import static io.choerodon.devops.app.eventhandler.constants.HarborRepoConstants.CUSTOM_REPO;
import static io.choerodon.devops.app.eventhandler.constants.HarborRepoConstants.DEFAULT_REPO;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.api.vo.harbor.HarborCustomRepo;
import io.choerodon.devops.infra.constant.HarborConstants;

/**
 * description
 *
 * @author mofei.li@hand-china.com 2020/06/11 10:24
 */
@ApiModel("Harbor仓库DTO")
public class HarborRepoDTO {

    @ApiModelProperty(value = "应用服务ID")
    private Long appServiceId;
    @ApiModelProperty(value = "猪齿鱼项目ID")
    private Long projectId;
    @ApiModelProperty(value = "仓库类型")
    private String repoType;
    @ApiModelProperty(value = "仓库配置信息")
    private HarborRepoConfigDTO harborRepoConfig;
    @ApiModelProperty(value = "仓库pull机器人账户")
    private HarborRepoRobotDTO pullRobot;
    @ApiModelProperty(value = "仓库push机器人账户")
    private HarborRepoRobotDTO pushRobot;

    public HarborRepoDTO() {
    }

    public HarborRepoDTO(Long appServiceId, Long projectId, String repoUrl, String repoName, List<HarborRobot> harborRobotList) {
        this.appServiceId = appServiceId;
        this.projectId = projectId;
        this.repoType = DEFAULT_REPO;
        this.harborRepoConfig.setRepoUrl(repoUrl);
        this.harborRepoConfig.setRepoName(repoName);
        harborRobotList.stream().forEach(harborRobot -> {
            if (harborRobot.getAction().equals(HarborConstants.HarborRobot.ACTION_PULL)) {
                this.pullRobot.setName(harborRobot.getName());
                this.pullRobot.setToken(harborRobot.getToken());
            } else {
                this.pushRobot.setName(harborRobot.getName());
                this.pushRobot.setToken(harborRobot.getToken());
            }
        });
    }

    public HarborRepoDTO(Long appServiceId, Long projectId, HarborCustomRepo harborCustomRepo) {
        this.appServiceId = appServiceId;
        this.projectId = projectId;
        this.repoType = CUSTOM_REPO;
        this.harborRepoConfig.setRepoUrl(harborCustomRepo.getRepoUrl());
        this.harborRepoConfig.setRepoName(harborCustomRepo.getRepoName());
        this.harborRepoConfig.setLoginName(harborCustomRepo.getLoginName());
        this.harborRepoConfig.setPassword(harborCustomRepo.getPassword());
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }

    public HarborRepoConfigDTO getHarborRepoConfig() {
        return harborRepoConfig;
    }

    public void setHarborRepoConfig(HarborRepoConfigDTO harborRepoConfig) {
        this.harborRepoConfig = harborRepoConfig;
    }

    public HarborRepoRobotDTO getPullRobot() {
        return pullRobot;
    }

    public void setPullRobot(HarborRepoRobotDTO pullRobot) {
        this.pullRobot = pullRobot;
    }

    public HarborRepoRobotDTO getPushRobot() {
        return pushRobot;
    }

    public void setPushRobot(HarborRepoRobotDTO pushRobot) {
        this.pushRobot = pushRobot;
    }
}
