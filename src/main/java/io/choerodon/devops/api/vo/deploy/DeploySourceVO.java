package io.choerodon.devops.api.vo.deploy;

import io.choerodon.devops.infra.enums.AppSourceType;

/**
 * Created by wangxiang on 2020/12/16
 */
public class DeploySourceVO {

    /**
     *  {@link AppSourceType}
     */
    private String type;

    /**
     * 共享的时候表明来自哪个projectName
     */
    private String projectName;

    /**
     * 市场应用的名称
     */
    private String marketAppName;

    /**
     * 市场服务名称
     */
    private String marketServiceName;

    private Long deployObjectId;

    public Long getDeployObjectId() {
        return deployObjectId;
    }

    public void setDeployObjectId(Long deployObjectId) {
        this.deployObjectId = deployObjectId;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getMarketAppName() {
        return marketAppName;
    }

    public void setMarketAppName(String marketAppName) {
        this.marketAppName = marketAppName;
    }

    public String getMarketServiceName() {
        return marketServiceName;
    }

    public void setMarketServiceName(String marketServiceName) {
        this.marketServiceName = marketServiceName;
    }
}
