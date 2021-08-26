package io.choerodon.devops.api.vo;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/7/13
 * @Modified By:
 */
public class EnvAutoDeployVO {
    private Boolean existAutoDeploy;
    private Boolean autoDeployStatus;

    public Boolean getExistAutoDeploy() {
        return existAutoDeploy;
    }

    public void setExistAutoDeploy(Boolean existAutoDeploy) {
        this.existAutoDeploy = existAutoDeploy;
    }

    public Boolean getAutoDeployStatus() {
        return autoDeployStatus;
    }

    public void setAutoDeployStatus(Boolean autoDeployStatus) {
        this.autoDeployStatus = autoDeployStatus;
    }
}
