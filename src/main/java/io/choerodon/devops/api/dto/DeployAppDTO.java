package io.choerodon.devops.api.dto;

import java.util.List;


public class DeployAppDTO {

    private String appName;
    private List<DeployAppDetail> deployAppDetails;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public List<DeployAppDetail> getDeployAppDetails() {
        return deployAppDetails;
    }

    public void setDeployAppDetails(List<DeployAppDetail> deployAppDetails) {
        this.deployAppDetails = deployAppDetails;
    }
}
