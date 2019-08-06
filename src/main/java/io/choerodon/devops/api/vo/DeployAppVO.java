package io.choerodon.devops.api.vo;

import java.util.List;


public class DeployAppVO {

    private String appServiceName;
    private List<DeployDetailVO> deployDetailVOS;

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public List<DeployDetailVO> getDeployDetailVOS() {
        return deployDetailVOS;
    }

    public void setDeployDetailVOS(List<DeployDetailVO> deployDetailVOS) {
        this.deployDetailVOS = deployDetailVOS;
    }
}
