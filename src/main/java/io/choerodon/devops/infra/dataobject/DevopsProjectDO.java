package io.choerodon.devops.infra.dataobject;

import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;

/**
 * Created by younger on 2018/3/29.
 */
@ModifyAudit
@Table(name = "devops_project")
public class DevopsProjectDO {

    @Id
    private Long id;
    private Integer gitlabGroupId;

    private String gitlabUuid;
    private String harborUuid;
    private String memberUuid;

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

    public String getGitlabUuid() {
        return gitlabUuid;
    }

    public void setGitlabUuid(String gitlabUuid) {
        this.gitlabUuid = gitlabUuid;
    }

    public String getHarborUuid() {
        return harborUuid;
    }

    public void setHarborUuid(String harborUuid) {
        this.harborUuid = harborUuid;
    }

    public String getMemberUuid() {
        return memberUuid;
    }

    public void setMemberUuid(String memberUuid) {
        this.memberUuid = memberUuid;
    }
}
