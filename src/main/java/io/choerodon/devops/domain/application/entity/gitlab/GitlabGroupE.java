package io.choerodon.devops.domain.application.entity.gitlab;

import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.infra.common.util.enums.Visibility;

/**
 * Created by younger on 2018/3/29.
 */
public class GitlabGroupE {
    private Integer id;
    private String path;
    private String name;
    private Visibility visibility;
    private ProjectE projectE;

    public GitlabGroupE() {

    }

    public GitlabGroupE(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public void initId(Integer id) {
        this.id = id;
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
