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
 * Date: 2018/11/21
 * Time: 11:08
 * Description:
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_app_service_user_rel")
public class AppServiceUserRelDTO extends BaseDTO {
    private Long iamUserId;
    private Long appServiceId;

    public AppServiceUserRelDTO() {
    }

    public AppServiceUserRelDTO(Long iamUserId, Long appServiceId) {
        this.iamUserId = iamUserId;
        this.appServiceId = appServiceId;
    }

    public Long getIamUserId() {
        return iamUserId;
    }

    public void setIamUserId(Long iamUserId) {
        this.iamUserId = iamUserId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }
}
