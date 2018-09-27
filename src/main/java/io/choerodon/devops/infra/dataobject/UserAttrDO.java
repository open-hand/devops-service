package io.choerodon.devops.infra.dataobject;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by zzy on 2018/3/26.
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_user")
public class UserAttrDO extends AuditDomain {

    @Id
    private Long id;

    @NotNull
    private Long gitlabUserId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGitlabUserId() {
        return gitlabUserId;
    }

    public void setGitlabUserId(Long gitlabUserId) {
        this.gitlabUserId = gitlabUserId;
    }
}
