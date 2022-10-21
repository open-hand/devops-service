package io.choerodon.devops.api.vo;

import java.util.List;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 添加集群的权限分配
 *
 * @author zmf
 */
public class DevopsClusterPermissionUpdateVO {
    @Encrypt
    @ApiModelProperty("集群id / 必需")
    @NotNull(message = "{devops.cluster.id.null}")
    private Long clusterId;

    @ApiModelProperty("要添加权限的项目id / 必需,可为空数组")
    @NotNull(message = "{devops.project.ids.null}")
    private List<Long> projectIds;

    @ApiModelProperty("是否跳过权限校验 / 必需")
    @NotNull(message = "{devops.skip.check.project.permission.null}")
    private Boolean skipCheckProjectPermission;

    @ApiModelProperty("集群的版本号, 如果更新了'skipCheckProjectPermission'字段则必填")
    private Long objectVersionNumber;

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public List<Long> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(List<Long> projectIds) {
        this.projectIds = projectIds;
    }

    public Boolean getSkipCheckProjectPermission() {
        return skipCheckProjectPermission;
    }

    public void setSkipCheckProjectPermission(Boolean skipCheckProjectPermission) {
        this.skipCheckProjectPermission = skipCheckProjectPermission;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
