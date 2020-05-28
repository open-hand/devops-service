package io.choerodon.devops.infra.dto;

import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by younger on 2018/3/29.
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_project")
public class DevopsProjectDTO extends AuditDomain {
    @Id
    private Long iamProjectId;
    private Long devopsAppGroupId;
    private Long devopsEnvGroupId;

    @ApiModelProperty("集群对应的环境所属的GitLab组ID")
    private Long devopsClusterEnvGroupId;

    private Boolean harborProjectIsPrivate;
    private String harborProjectUserName;
    private String harborProjectUserPassword;
    private String harborProjectUserEmail;
    private Long harborUserId;
    private Long harborPullUserId;

    public DevopsProjectDTO() {
    }

    public DevopsProjectDTO(Long iamProjectId) {
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

    public String getHarborProjectUserName() {
        return harborProjectUserName;
    }

    public void setHarborProjectUserName(String harborProjectUserName) {
        this.harborProjectUserName = harborProjectUserName;
    }

    public String getHarborProjectUserPassword() {
        return harborProjectUserPassword;
    }

    public void setHarborProjectUserPassword(String harborProjectUserPassword) {
        this.harborProjectUserPassword = harborProjectUserPassword;
    }

    public String getHarborProjectUserEmail() {
        return harborProjectUserEmail;
    }

    public void setHarborProjectUserEmail(String harborProjectUserEmail) {
        this.harborProjectUserEmail = harborProjectUserEmail;
    }

    public Boolean getHarborProjectIsPrivate() {
        return harborProjectIsPrivate;
    }

    public void setHarborProjectIsPrivate(Boolean harborProjectIsPrivate) {
        this.harborProjectIsPrivate = harborProjectIsPrivate;
    }

    public Long getHarborPullUserId() {
        return harborPullUserId;
    }

    public void setHarborPullUserId(Long harborPullUserId) {
        this.harborPullUserId = harborPullUserId;
    }

    public Long getHarborUserId() {
        return harborUserId;
    }

    public void setHarborUserId(Long harborUserId) {
        this.harborUserId = harborUserId;
    }

    public Long getDevopsClusterEnvGroupId() {
        return devopsClusterEnvGroupId;
    }

    public void setDevopsClusterEnvGroupId(Long devopsClusterEnvGroupId) {
        this.devopsClusterEnvGroupId = devopsClusterEnvGroupId;
    }

    @Override
    public String toString() {
        return "DevopsProjectDTO{" +
                "iamProjectId=" + iamProjectId +
                ", devopsAppGroupId=" + devopsAppGroupId +
                ", devopsEnvGroupId=" + devopsEnvGroupId +
                ", devopsClusterEnvGroupId=" + devopsClusterEnvGroupId +
                ", harborProjectIsPrivate=" + harborProjectIsPrivate +
                ", harborProjectUserName='" + harborProjectUserName + '\'' +
                ", harborProjectUserPassword='" + harborProjectUserPassword + '\'' +
                ", harborProjectUserEmail='" + harborProjectUserEmail + '\'' +
                ", harborUserId=" + harborUserId +
                ", harborPullUserId=" + harborPullUserId +
                '}';
    }
}
