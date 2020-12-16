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

    private Long appServiceId;

    /**
     *   应用服务版本id
     */
    private Long appServiceVersionId;

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getAppServiceVersionId() {
        return appServiceVersionId;
    }

    public void setAppServiceVersionId(Long appServiceVersionId) {
        this.appServiceVersionId = appServiceVersionId;
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
