package io.choerodon.devops.infra.dataobject;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by Zenger on 2018/4/14.
 */
@VersionAudit
@ModifyAudit
@Table(name = "devops_service_app_instance")
public class DevopsServiceAppInstanceDO extends AuditDomain {

    @Id
    @GeneratedValue
    private Long id;
    private Long serviceId;
    private Long appInstanceId;
    private String code;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getAppInstanceId() {
        return appInstanceId;
    }

    public void setAppInstanceId(Long appInstanceId) {
        this.appInstanceId = appInstanceId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
