package io.choerodon.devops.api.vo;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
public class DevopsProjectConfigDTO {

    private Long id;

    private String name;

    private ProjectConfigDTO config;

    private Long projectId;

    private String type;

    private Long objectVersionNumber;

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
