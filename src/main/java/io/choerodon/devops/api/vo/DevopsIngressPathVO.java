package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Creator: Runge
 * Date: 2018/4/20
 * Time: 15:43
 * Description:
 */
public class DevopsIngressPathVO {
    @ApiModelProperty("Ingress的path值")
    private String path;
    @ApiModelProperty("path对应的网络id")
    @Encrypt
    private Long serviceId;
    @ApiModelProperty("网络名称")
    private String serviceName;
    @ApiModelProperty("网络状态")
    private String serviceStatus;
    @ApiModelProperty("网络的错误信息")
    private String serviceError;
    @ApiModelProperty("网络端口")
    private Long servicePort;


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

    public Long getServicePort() {
        return servicePort;
    }

    public void setServicePort(Long servicePort) {
        this.servicePort = servicePort;
    }

    public String getServiceError() {
        return serviceError;
    }

    public void setServiceError(String serviceError) {
        this.serviceError = serviceError;
    }

    @Override
    public String toString() {
        return "DevopsIngressPathVO{" +
                "path='" + path + '\'' +
                ", serviceId=" + serviceId +
                ", serviceName='" + serviceName + '\'' +
                ", serviceStatus='" + serviceStatus + '\'' +
                ", serviceError='" + serviceError + '\'' +
                ", servicePort=" + servicePort +
                '}';
    }
}
