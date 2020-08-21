package io.choerodon.devops.infra.dto;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Zenger on 2018/4/14.
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_service_instance")
public class DevopsServiceInstanceDTO extends AuditDomain {

    public static final String ENCRYPT_KEY = "devops_service_instance";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
//    @Encrypt(DevopsServiceInstanceDTO.ENCRYPT_KEY)
    private Long id;
//    @Encrypt(AppServiceDTO.ENCRYPT_KEY)
    private Long serviceId;
//    @Encrypt(DevopsServiceInstanceDTO.ENCRYPT_KEY)
    private Long instanceId;
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

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
