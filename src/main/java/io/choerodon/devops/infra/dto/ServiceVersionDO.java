package io.choerodon.devops.infra.dto;

import java.util.List;

/**
 * Created by Zenger on 2018/4/25.
 */
public class ServiceVersionDO {

    private Long id;
    private String version;
    private List<ServiceInstanceDO> appInstance;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<ServiceInstanceDO> getAppInstance() {
        return appInstance;
    }

    public void setAppInstance(List<ServiceInstanceDO> appInstance) {
        this.appInstance = appInstance;
    }
}
