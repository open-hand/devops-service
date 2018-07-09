package io.choerodon.devops.domain.application.entity;

import io.choerodon.devops.domain.application.valueobject.ProjectHook;

public class DevopsAppWebHookE {

    private Long id;
    private ApplicationE applicationE;
    private ProjectHook projectHook;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApplicationE getApplicationE() {
        return applicationE;
    }

    public void initApplicationE(Long id) {
        this.applicationE = new ApplicationE(id);
    }

    public ProjectHook getProjectHook() {
        return projectHook;
    }

    public void initProjectHook(Integer id) {
        this.projectHook = new ProjectHook(id);
    }
}
