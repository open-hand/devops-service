package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

public class DevopsHostUserPermissionDeleteResultVO {

    @ApiModelProperty("true表示前端需要刷新整个主机管理页面")
    private Boolean refreshAll;

    public DevopsHostUserPermissionDeleteResultVO(Boolean refreshAll) {
        this.refreshAll = refreshAll;
    }

    public Boolean getRefreshAll() {
        return refreshAll;
    }

    public void setRefreshAll(Boolean refreshAll) {
        this.refreshAll = refreshAll;
    }
}
