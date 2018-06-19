package io.choerodon.devops.infra.dataobject;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;

/**
 * Creator: Runge
 * Date: 2018/6/19
 * Time: 11:10
 * Description:
 */
@ModifyAudit
@Table(name = "devops_app_version_readme")
public class ApplicationVersionReadmeDO {
    @Id
    @GeneratedValue
    private Long id;
    private Long versionId;
    private String readme;

    public ApplicationVersionReadmeDO() {
    }

    public ApplicationVersionReadmeDO(Long versionId) {
        this.versionId = versionId;
    }

    public ApplicationVersionReadmeDO(Long versionId, String readme) {

        this.versionId = versionId;
        this.readme = readme;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getReadme() {
        return readme;
    }

    public void setReadme(String readme) {
        this.readme = readme;
    }
}
