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
 * Date: 2018/11/21
 * Time: 11:08
 * Description:
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_app_service_user_rel")
public class AppServiceUserRelDTO extends AuditDomain {
    // 这个表没有主键，这个@Id注解是防止启动报错
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long iamUserId;
    private Long appServiceId;

    public AppServiceUserRelDTO() {
    }

    public AppServiceUserRelDTO(Long iamUserId, Long appServiceId) {
        this.iamUserId = iamUserId;
        this.appServiceId = appServiceId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
