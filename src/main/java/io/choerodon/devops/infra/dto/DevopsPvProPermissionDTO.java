package io.choerodon.devops.infra.dto;

import javax.persistence.Table;

@Table(name = "devops_pv_project_rel")
public class DevopsPvProPermissionDTO {

    private Long pvId;

    private Long projectId;

    public Long getPvId() {
        return pvId;
    }

    public void setPvId(Long pvId) {
        this.pvId = pvId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
