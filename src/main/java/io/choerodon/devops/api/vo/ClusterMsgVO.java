package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * User: Mr.Wang
 * Date: 2019/11/19
 */
public class ClusterMsgVO {
    @ApiModelProperty("集群下是否存在关联环境")
    private Boolean checkEnv;
    @ApiModelProperty("集群下是否存在PV")
    private Boolean checkPV;

    public ClusterMsgVO(){

    }
    public ClusterMsgVO(Boolean checkEnv, Boolean checkPV) {
        this.checkEnv = checkEnv;
        this.checkPV = checkPV;
    }

    public Boolean getCheckEnv() {
        return checkEnv;
    }

    public void setCheckEnv(Boolean checkEnv) {
        this.checkEnv = checkEnv;
    }

    public Boolean getCheckPV() {
        return checkPV;
    }

    public void setCheckPV(Boolean checkPV) {
        this.checkPV = checkPV;
    }
}
