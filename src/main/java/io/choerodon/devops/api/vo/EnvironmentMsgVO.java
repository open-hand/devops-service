package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * User: Mr.Wang
 * Date: 2019/11/19
 */
public class EnvironmentMsgVO {
    @ApiModelProperty("环境中是否有资源")
    private Boolean checkResources;

    @ApiModelProperty("type为null表示查询环境是否存在")
    private Boolean checkEnvExist;

    @ApiModelProperty("type为app时查询应用服务是否存在")
    private Boolean checkAppExist;

    public EnvironmentMsgVO() {

    }

    public EnvironmentMsgVO(Boolean checkResources, Boolean checkEnvExist,Boolean checkAppExist) {
        this.checkResources = checkResources;
        this.checkEnvExist = checkEnvExist;
        this.checkAppExist=checkAppExist;
    }

    public Boolean getCheckResources() {
        return checkResources;
    }

    public void setCheckResources(Boolean checkResources) {
        this.checkResources = checkResources;
    }

    public Boolean getCheckEnvExist() {
        return checkEnvExist;
    }

    public void setCheckEnvExist(Boolean checkEnvExist) {
        this.checkEnvExist = checkEnvExist;
    }

    public Boolean getCheckAppExist() {
        return checkAppExist;
    }

    public void setCheckAppExist(Boolean checkAppExist) {
        this.checkAppExist = checkAppExist;
    }
}
