package io.choerodon.devops.infra.dataobject;

import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by younger on 2018/3/29.
 */
@ModifyAudit
@Table(name = "devops_project")
public class DevopsProjectDO extends AuditDomain {

    @Id
    private Long iamProjectId;
    private Long devopsAppGroupId;
    private Long devopsEnvGroupId;

    public DevopsProjectDO() {

    }

    public DevopsProjectDO(Long iamProjectId) {
        this.iamProjectId = iamProjectId;
    }

    public Long getIamProjectId() {
        return iamProjectId;
    }

    public void setIamProjectId(Long iamProjectId) {
        this.iamProjectId = iamProjectId;
    }

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
}
