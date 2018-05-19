package io.choerodon.devops.domain.application.entity;

/**
 * Created by Zenger on 2018/4/14.
 */
public class DevopsServiceAppInstanceE {

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

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }
}
