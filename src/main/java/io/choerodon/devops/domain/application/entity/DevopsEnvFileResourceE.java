package io.choerodon.devops.domain.application.entity;

import java.io.File;

/**
 * Creator: Runge
 * Date: 2018/7/25
 * Time: 16:18
 * Description:
 */
public class DevopsEnvFileResourceE {
    private Long id;
    private DevopsEnvironmentE environment;
    private String filePath;
    private File file;
    private String resourceType;
    private Long resourceId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DevopsEnvironmentE getEnvironment() {
        return environment;
    }

    public void setEnvironment(DevopsEnvironmentE environment) {
        this.environment = environment;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;

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
