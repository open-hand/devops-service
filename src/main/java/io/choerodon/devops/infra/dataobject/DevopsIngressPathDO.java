package io.choerodon.devops.infra.dataobject;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Creator: Runge
 * Date: 2018/4/20
 * Time: 14:30
 * Description:
 */
@VersionAudit
@ModifyAudit
@Table(name = "devops_ingress_path")
public class DevopsIngressPathDO extends AuditDomain {

    @Id
    @GeneratedValue
    private Long id;
    private Long ingressId;
    private String path;
    private Long serviceId;
    private String serviceName;

    public DevopsIngressPathDO() {
    }

    public DevopsIngressPathDO(Long ingressId) {
        this.ingressId = ingressId;
    }

    public DevopsIngressPathDO(Long ingressId, String path) {
        this.ingressId = ingressId;
        this.path = path;
    }

    /**
     * 构造函数
     */
    public DevopsIngressPathDO(Long ingressId, String path, Long serviceId, String serviceName) {
        this.ingressId = ingressId;
        this.path = path;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
    }

    /**
     * 构造函数
     */
    public DevopsIngressPathDO(Long id, Long ingressId, String path, Long serviceId) {
        this.id = id;
        this.ingressId = ingressId;
        this.path = path;
        this.serviceId = serviceId;
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Long getIngressId() {
        return ingressId;
    }

    public void setIngressId(Long ingressId) {
        this.ingressId = ingressId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
