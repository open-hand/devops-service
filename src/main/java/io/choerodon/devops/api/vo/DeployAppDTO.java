package io.choerodon.devops.api.vo;

import java.util.List;


public class DeployAppDTO {

    private String appName;
    private List<DeployDetailVO> deployDetailVOS;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public List<DeployDetailVO> getDeployDetailVOS() {
        return deployDetailVOS;
    }

    public void setDeployDetailVOS(List<DeployDetailVO> deployDetailVOS) {
        this.deployDetailVOS = deployDetailVOS;
    }
}
