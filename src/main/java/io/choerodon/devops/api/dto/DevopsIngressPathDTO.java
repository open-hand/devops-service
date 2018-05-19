package io.choerodon.devops.api.dto;

import java.util.Objects;

/**
 * Creator: Runge
 * Date: 2018/4/20
 * Time: 15:43
 * Description:
 */
public class DevopsIngressPathDTO {

    private String path;
    private Long serviceId;
    private String serviceName;
    private String serviceStatus;

    public DevopsIngressPathDTO() {
    }

    /**
     * 构造函数
     */
    public DevopsIngressPathDTO(String path, Long serviceId, String serviceName, String serviceStatus) {
        this.path = path;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.serviceStatus = serviceStatus;
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

    public String getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(String serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DevopsIngressPathDTO that = (DevopsIngressPathDTO) o;
        return Objects.equals(path, that.path)
                && Objects.equals(serviceId, that.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, serviceId);
    }
}
