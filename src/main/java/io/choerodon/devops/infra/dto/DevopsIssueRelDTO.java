package io.choerodon.devops.infra.dto;

import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

@ModifyAudit
@VersionAudit
@Table(name = "devops_issue_rel")
public class DevopsIssueRelDTO extends AuditDomain {

    @ApiModelProperty("关联对象 分支或提交")
    private String object;

    @ApiModelProperty("关联对象id")
    private Long objectId;

    @ApiModelProperty("敏捷的issueId")
    private Long issueId;

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }
}
