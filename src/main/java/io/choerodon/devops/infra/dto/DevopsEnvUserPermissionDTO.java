package io.choerodon.devops.infra.dto;

import javax.persistence.Table;

<<<<<<< HEAD
import io.choerodon.mybatis.entity.BaseDTO;
=======
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
>>>>>>> [ADD] add ModifyAudit VersionAudit for table dto

/**
 * Created by n!Ck
 * Date: 2018/10/25
 * Time: 16:52
 * Description:
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_env_user_permission")
public class DevopsEnvUserPermissionDTO extends BaseDTO {
    private String loginName;
<<<<<<< HEAD
=======
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

>>>>>>> [UPD] update strategy of @GeneratedValue to GenerationType.AUTO
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
