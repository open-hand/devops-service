package io.choerodon.devops.infra.dataobject;

import javax.persistence.Table;

/**
 * @author lizongwei
 * @date 2019/7/3
 */
@Table(name = "devops_app_resource")
public class DevopsAppResourceDO {

    private Long appId;
    private String resourceType;
    private Long resourceId;

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
}
