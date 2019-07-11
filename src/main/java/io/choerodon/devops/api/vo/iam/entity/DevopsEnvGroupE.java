package io.choerodon.devops.api.vo.iam.entity;

import io.choerodon.devops.api.vo.ProjectVO;

public class DevopsEnvGroupE {

    private Long id;
    private ProjectVO projectE;
    private String name;
    private Long sequence;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProjectVO getProjectE() {
        return projectE;
    }

    public void initProject(Long projectId) {
        this.projectE = new ProjectVO(projectId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProjectE(ProjectVO projectE) {
        this.projectE = projectE;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }
}
