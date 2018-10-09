package io.choerodon.devops.domain.application.entity.gitlab;

import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.infra.common.util.enums.Visibility;

/**
 * Created by younger on 2018/3/29.
 */
public class GitlabGroupE {
    private Long devopsAppGroupId;
    private Long devopsEnvGroupId;
    private String path;
    private String name;
    private Visibility visibility;
    private ProjectE projectE;


    public Long getDevopsAppGroupId() {
        return devopsAppGroupId;
    }

    public void setDevopsAppGroupId(Long devopsAppGroupId) {
        this.devopsAppGroupId = devopsAppGroupId;
    }

    public Long getDevopsEnvGroupId() {
        return devopsEnvGroupId;
    }

    public void setDevopsEnvGroupId(Long devopsEnvGroupId) {
        this.devopsEnvGroupId = devopsEnvGroupId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProjectE getProjectE() {
        return projectE;
    }

    public void setProjectE(ProjectE projectE) {
        this.projectE = projectE;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public void initProjectE(Long id) {
        this.projectE = new ProjectE(id);
    }

    public void initName(String name) {
        this.name = name;
    }

    public void initPath(String path) {
        this.path = path;
    }

    public void initVisibility(Visibility visibility) {
        this.visibility = visibility;
    }
}
