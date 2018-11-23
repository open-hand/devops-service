package io.choerodon.devops.infra.dataobject;

import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

@VersionAudit
@ModifyAudit
@Table(name = "devops_cluster_pro_rel")
public class DevopsClusterProPermissionDO extends AuditDomain {

    @Id
    private Long clusterId;
    private Long projectId;

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
