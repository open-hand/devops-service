package io.choerodon.devops.api.vo;

import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 更新环境的权限分配
 *
 * @author zmf
 */
public class DevopsEnvPermissionUpdateVO {
    @ApiModelProperty("环境id / 必需")
    @NotNull(message = "error.env.id.null")
    @Encrypt(DevopsEnvironmentDTO.ENCRYPT_KEY)
    private Long envId;

    @ApiModelProperty("要添加权限的用户id / 必需")
    @NotNull(message = "error.user.ids.null")
    private List<Long> userIds;

    @ApiModelProperty("是否跳过权限校验 / 必需")
    @NotNull(message = "error.is.skip.permission.check.null")
    private Boolean skipCheckPermission;

    @ApiModelProperty("环境的版本号, 如果更新了'skipCheckPermission'字段则必填")
    private Long objectVersionNumber;

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public Boolean getSkipCheckPermission() {
        return skipCheckPermission;
    }

    public void setSkipCheckPermission(Boolean skipCheckPermission) {
        this.skipCheckPermission = skipCheckPermission;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
