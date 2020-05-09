package io.choerodon.devops.infra.dto;

import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by n!Ck
 * Date: 2018/10/25
 * Time: 16:52
 * Description:
 */

@Table(name = "devops_env_user_permission")
public class DevopsEnvUserPermissionDTO extends AuditDomain {
    private String loginName;
    // 这个表没有主键，这个@Id注解是防止启动报错
    @Id
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
