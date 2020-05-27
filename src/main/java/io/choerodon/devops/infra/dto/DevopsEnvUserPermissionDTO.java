package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by n!Ck
 * Date: 2018/10/25
 * Time: 16:52
 * Description:
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_env_user_permission")
public class DevopsEnvUserPermissionDTO extends AuditDomain {
    private String loginName;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long iamUserId;
    private String realName;
    private Long envId;
    private Boolean isPermitted;

    public DevopsEnvUserPermissionDTO() {
    }

    public DevopsEnvUserPermissionDTO(Long envId, Long userId) {
        this.envId = envId;
        this.iamUserId = userId;
    }

    public DevopsEnvUserPermissionDTO(String loginName, Long iamUserId, String realName, Long envId, Boolean isPermitted) {
        this.loginName = loginName;
        this.iamUserId = iamUserId;
        this.realName = realName;
        this.envId = envId;
        this.isPermitted = isPermitted;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public Long getIamUserId() {
        return iamUserId;
    }

    public void setIamUserId(Long iamUserId) {
        this.iamUserId = iamUserId;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Boolean getPermitted() {
        return isPermitted;
    }

    public void setPermitted(Boolean permitted) {
        isPermitted = permitted;
    }
}
