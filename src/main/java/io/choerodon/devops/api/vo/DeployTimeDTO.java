package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

public class DeployTimeDTO {

    private List<Date> creationDates;
    private List<DeployAppDTO> deployAppDTOS;


    public List<Date> getCreationDates() {
        return creationDates;
    }

    public void setCreationDates(List<Date> creationDates) {
        this.creationDates = creationDates;
    }

    public List<DeployAppDTO> getDeployAppDTOS() {
        return deployAppDTOS;
    }

    public void setDeployAppDTOS(List<DeployAppDTO> deployAppDTOS) {
        this.deployAppDTOS = deployAppDTOS;
    }
}
