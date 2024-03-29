package io.choerodon.devops.api.vo;

import java.util.List;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.enums.DevopsHostUserPermissionLabelEnums;

public class DevopsHostUserPermissionUpdateVO {
    @Encrypt
    @ApiModelProperty("主机id / 必需")
    @NotNull(message = "error.env.id.null")
    private Long hostId;

    @Encrypt
    @ApiModelProperty("要添加权限的用户id / 必需")
    @NotNull(message = "error.user.ids.null")
    private List<Long> userIds;

    /**
     * {@link DevopsHostUserPermissionLabelEnums}
     */
    @ApiModelProperty("用户在主机下的权限标签")
    private String permissionLabel;

    @ApiModelProperty("主机的版本号, 如果更新了'skipCheckPermission'字段则必填")
    private Long objectVersionNumber;

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public String getPermissionLabel() {
        return permissionLabel;
    }

    public void setPermissionLabel(String permissionLabel) {
        this.permissionLabel = permissionLabel;
    }
}
