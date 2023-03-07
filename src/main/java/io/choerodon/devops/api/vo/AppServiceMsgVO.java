package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * User: Mr.Wang
 * Date: 2019/11/19
 */
public class AppServiceMsgVO {
    @ApiModelProperty("该应用服务下是否存在关联资源")
    private Boolean checkResources;
    @ApiModelProperty("该应用服务下是否存在共享规则")
    private Boolean checkRule;
    @ApiModelProperty("该应用服务下是否存在ci流水线")
    private Boolean checkCi;

    @ApiModelProperty("该应用服务下是否存在持续部署流程")
    private Boolean checkCd;

    public AppServiceMsgVO(Boolean checkResources, Boolean checkRule) {
        this.checkResources = checkResources;
        this.checkRule = checkRule;
    }

    public Boolean getCheckCd() {
        return checkCd;
    }

    public void setCheckCd(Boolean checkCd) {
        this.checkCd = checkCd;
    }

    public Boolean getCheckResources() {
        return checkResources;
    }

    public void setCheckResources(Boolean checkResources) {
        this.checkResources = checkResources;
    }

    public Boolean getCheckRule() {
        return checkRule;
    }

    public void setCheckRule(Boolean checkRule) {
        this.checkRule = checkRule;
    }

    public Boolean getCheckCi() {
        return checkCi;
    }

    public void setCheckCi(Boolean checkCi) {
        this.checkCi = checkCi;
    }
}
