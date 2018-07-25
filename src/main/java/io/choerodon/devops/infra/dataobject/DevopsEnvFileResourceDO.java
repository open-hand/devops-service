package io.choerodon.devops.infra.dataobject;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Creator: Runge
 * Date: 2018/7/25
 * Time: 15:34
 * Description:
 */
public class DevopsEnvFileResourceDO {
    @Id
    @GeneratedValue
    private Long id;
    private Long envId;
    private String filePath;
    private String resourceType;
    private Long resourceId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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
