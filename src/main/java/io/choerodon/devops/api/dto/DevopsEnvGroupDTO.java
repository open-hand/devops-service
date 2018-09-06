package io.choerodon.devops.api.dto;

/**
 * Creator: Runge
 * Date: 2018/9/4
 * Time: 14:49
 * Description:
 */
public class DevopsEnvGroupDTO {
    private Long id;
    private Long projectId;
    private String name;
    private Long sequence;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }
}
