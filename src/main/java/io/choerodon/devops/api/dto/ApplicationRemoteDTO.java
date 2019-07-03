package io.choerodon.devops.api.dto;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:49 2019/7/3
 * Description:
 */
public class ApplicationRemoteDTO {
    private Long id;
    private String name;
    private String code;
    private Long projectId;
    private String type;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
}
