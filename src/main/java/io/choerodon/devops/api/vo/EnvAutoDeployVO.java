package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/7/13
 * @Modified By:
 */
public class EnvAutoDeployVO {
    @ApiModelProperty("是否存在自动部署")
    private Boolean existAutoDeploy;
    @ApiModelProperty("是否启用自动部署")
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
