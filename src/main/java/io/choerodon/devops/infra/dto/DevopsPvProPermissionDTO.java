package io.choerodon.devops.infra.dto;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "devops_pv_project_rel")
public class DevopsPvProPermissionDTO {

    private Long pvId;

    // 这个表没有主键，这个@Id注解是防止启动报错
    @Id
    private Long projectId;

    @Transient
    private Long clusterId;

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

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
