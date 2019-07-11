package io.choerodon.devops.domain.application.entity;

public class DevopsIngressPathE {

    private Long id;
    private DevopsIngressE devopsIngressE;
    private String path;
    private Long serviceId;
    private String serviceName;
    private Long servicePort;
    private Long objectVersionNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DevopsIngressE getDevopsIngressE() {
        return devopsIngressE;
    }

    public void setDevopsIngressE(DevopsIngressE devopsIngressE) {
        this.devopsIngressE = devopsIngressE;
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

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public void initDevopsIngressE(Long id) {
        this.devopsIngressE = new DevopsIngressE(id);
    }

    public Long getServicePort() {
        return servicePort;
    }

    public void setServicePort(Long servicePort) {
        this.servicePort = servicePort;
    }
}
