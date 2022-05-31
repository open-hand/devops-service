package io.choerodon.devops.api.vo.deploy;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.enums.AppSourceType;

/**
 * Created by wangxiang on 2020/12/16
 */
public class DeploySourceVO {

    /**
     * {@link AppSourceType}
     */
    @ApiModelProperty("来源类型")
    private String type;
    @ApiModelProperty("共享的时候表明来自哪个projectName")
    private String projectName;
    @ApiModelProperty("市场应用的名称")
    private String marketAppName;
    @ApiModelProperty("市场服务名称")
    private String marketServiceName;
    @ApiModelProperty("部署对象id")
    private Long deployObjectId;

    public DeploySourceVO() {
    }

    public DeploySourceVO(AppSourceType type, String projectName) {
        this.type = type.getValue();
        this.projectName = projectName;
    }

    public Long getDeployObjectId() {
        return deployObjectId;
    }

    public DeploySourceVO setDeployObjectId(Long deployObjectId) {
        this.deployObjectId = deployObjectId;
        return this;
    }


    public String getType() {
        return type;
    }

    public DeploySourceVO setType(String type) {
        this.type = type;
        return this;
    }

    public String getProjectName() {
        return projectName;
    }

    public DeploySourceVO setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public String getMarketAppName() {
        return marketAppName;
    }

    public DeploySourceVO setMarketAppName(String marketAppName) {
        this.marketAppName = marketAppName;
        return this;
    }

    public String getMarketServiceName() {
        return marketServiceName;
    }

    public DeploySourceVO setMarketServiceName(String marketServiceName) {
        this.marketServiceName = marketServiceName;
        return this;
    }
}
