package io.choerodon.devops.domain.application.entity;

import io.choerodon.devops.api.vo.ProjectConfigDTO;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
public class DevopsProjectConfigE {
    private Long id;
    private String name;
    private ProjectConfigDTO config;
    private Long projectId;
    private String type;
    private Long objectVersionNumber;

    public DevopsProjectConfigE() {
    }


    public DevopsProjectConfigE(String name, ProjectConfigDTO config, String type) {
        this.name = name;
        this.config = config;
        this.type = type;
    }

    public DevopsProjectConfigE(Long id) {
        this.id = id;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProjectConfigDTO getConfig() {
        return config;
    }

    public void setConfig(ProjectConfigDTO config) {
        this.config = config;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
