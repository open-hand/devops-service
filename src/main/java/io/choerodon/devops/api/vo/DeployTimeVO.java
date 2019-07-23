package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

public class DeployTimeVO {

    private List<Date> creationDates;
    private List<DeployAppVO> deployAppVOS;


    public List<Date> getCreationDates() {
        return creationDates;
    }

    public void setCreationDates(List<Date> creationDates) {
        this.creationDates = creationDates;
    }

    public List<DeployAppVO> getDeployAppVOS() {
        return deployAppVOS;
    }

    public void setDeployAppVOS(List<DeployAppVO> deployAppVOS) {
        this.deployAppVOS = deployAppVOS;
    }
}
