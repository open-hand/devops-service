package io.choerodon.devops.infra.dto;

import io.choerodon.mybatis.entity.BaseDTO;

import javax.persistence.Table;

@Table(name = "devops_pv_project_rel")
public class DevopsPvProPermissionDTO extends BaseDTO {

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
