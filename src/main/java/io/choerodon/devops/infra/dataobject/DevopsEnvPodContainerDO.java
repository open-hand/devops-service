package io.choerodon.devops.infra.dataobject;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Creator: Runge
 * Date: 2018/5/8
 * Time: 15:44
 * Description:
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_env_pod_container")
public class DevopsEnvPodContainerDO extends AuditDomain {
    @Id
    @GeneratedValue
    private Long id;
    private Long podId;
    private String containerName;

    public DevopsEnvPodContainerDO() {
    }

    public DevopsEnvPodContainerDO(Long podId) {
        this.podId = podId;
    }

    public DevopsEnvPodContainerDO(Long podId, String containerName) {
        this.podId = podId;
        this.containerName = containerName;
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPodId() {
        return podId;
    }

    public void setPodId(Long podId) {
        this.podId = podId;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }
}
