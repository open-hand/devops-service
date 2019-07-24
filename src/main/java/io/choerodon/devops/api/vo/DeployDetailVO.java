package io.choerodon.devops.api.vo;

import java.util.Date;

public class DeployDetailVO {

    private Date deployDate;
    private String deployTime;

    public Date getDeployDate() {
        return deployDate;
    }

    public void setDeployDate(Date deployDate) {
        this.deployDate = deployDate;
    }

    public String getDeployTime() {
        return deployTime;
    }

    public void setDeployTime(String deployTime) {
        this.deployTime = deployTime;
    }
}
