package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 用户与主机权限关系表
 */
@Table(name = "devops_host_user_permission")
@ModifyAudit
@VersionAudit
public class DevopsHostUserPermissionDTO extends AuditDomain {
    @Id
    @GeneratedValue
    private Long id;
    @ApiModelProperty("主机id")
    private Long hostId;
    @ApiModelProperty("用户id")
    private Long iamUserId;
    @ApiModelProperty("权限标签")
    private String permissionLabel;

    public DevopsHostUserPermissionDTO() {
    }

    public DevopsHostUserPermissionDTO(Long hostId, Long userId) {
        this.hostId = hostId;
        this.iamUserId = userId;
    }

    public DevopsHostUserPermissionDTO(String loginName, Long iamUserId, Long hostId, String realName, String permissionLabel) {
        this.iamUserId = iamUserId;
        this.hostId = hostId;
        this.permissionLabel = permissionLabel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public Long getIamUserId() {
        return iamUserId;
    }

    public void setIamUserId(Long iamUserId) {
        this.iamUserId = iamUserId;
    }

    public String getPermissionLabel() {
        return permissionLabel;
    }

    public void setPermissionLabel(String permissionLabel) {
        this.permissionLabel = permissionLabel;
    }
}
