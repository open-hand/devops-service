package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class UserAppServiceIdsVO {
    @ApiModelProperty(name = "用户id")
    private Long userId;
    @ApiModelProperty(name = "拥有权限的应用id")
    private List<Long> appServiceIds;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<Long> getAppServiceIds() {
        return appServiceIds;
    }

    public void setAppServiceIds(List<Long> appServiceIds) {
        this.appServiceIds = appServiceIds;
    }
}
