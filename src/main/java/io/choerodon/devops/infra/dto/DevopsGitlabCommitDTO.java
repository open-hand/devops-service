package io.choerodon.devops.infra.dto;

import java.util.Date;
import java.util.List;
import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

@ModifyAudit
@VersionAudit
@Table(name = "devops_gitlab_commit")
public class DevopsGitlabCommitDTO extends AuditDomain {

    public static final String ENCRYPT_KEY = "devops_gitlab_commit";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("应用服务Id")
    private Long appServiceId;
    @ApiModelProperty("用户Id")
    private Long userId;
    @Deprecated
    @ApiModelProperty("敏捷的issueId")
    private Long issueId;
    @ApiModelProperty("commit sha")
    private String commitSha;
    @ApiModelProperty("提交内容")
    private String commitContent;
    @ApiModelProperty("提交分支")
    private String ref;
    @ApiModelProperty("提交日期")
    private Date commitDate;

    @Transient
    @ApiModelProperty("应用服务名称")
    private String appServiceName;
    @Transient
    @ApiModelProperty("应用服务编码")
    private String appServiceCode;
    @ApiModelProperty("gitlab url")
    private String url;

    @ApiModelProperty("敏捷的issueId")
    @Transient
    private List<Long> issueIds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }

    public String getCommitContent() {
        return commitContent;
    }

    public void setCommitContent(String commitContent) {
        this.commitContent = commitContent;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Date getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(Date commitDate) {
        this.commitDate = commitDate;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAppServiceCode() {
        return appServiceCode;
    }

    public void setAppServiceCode(String appServiceCode) {
        this.appServiceCode = appServiceCode;
    }
}
