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
    private Long id;
    private Integer gitlabGroupId;
    private Integer envGroupId;

    public DevopsProjectDO() {

    }

    public DevopsProjectDO(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getGitlabGroupId() {
        return gitlabGroupId;
    }

    public void setGitlabGroupId(Integer gitlabGroupId) {
        this.gitlabGroupId = gitlabGroupId;
    }

    public Integer getEnvGroupId() {
        return envGroupId;
    }

    public void setEnvGroupId(Integer envGroupId) {
        this.envGroupId = envGroupId;
    }
}
