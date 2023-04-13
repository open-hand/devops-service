package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.core.base.BaseConstants;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * @author scp
 * @date 2020/7/22
 * @description
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_ci_pipeline_maven")
public class CiPipelineMavenDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("应用服务id")
    @Encrypt
    private Long appServiceId;
    private Long gitlabPipelineId;
    private String jobName;
    private String groupId;
    private String artifactId;
    private String version;
    private Long nexusRepoId;
    private String mavenRepoUrl;
    private String username;
    private String password;
    private String artifactType;

    public String calculateDownloadUrl() {
        String downloadUrl = "";
        // SNAPSHOT类型
        if (getVersion().contains(BaseConstants.Symbol.SLASH)) {
            downloadUrl = appendWithSlash(getMavenRepoUrl(), getGroupId().replace(BaseConstants.Symbol.POINT, BaseConstants.Symbol.SLASH));
            downloadUrl = appendWithSlash(downloadUrl, getArtifactId());
            downloadUrl = appendWithSlash(downloadUrl, getVersion() + "." + artifactType);
        } else {
            // RELEASE类型
            downloadUrl = appendWithSlash(getMavenRepoUrl(), getGroupId().replace(BaseConstants.Symbol.POINT, BaseConstants.Symbol.SLASH));
            downloadUrl = appendWithSlash(downloadUrl, getArtifactId());
            downloadUrl = appendWithSlash(downloadUrl, getVersion());
            downloadUrl = appendWithSlash(downloadUrl, getArtifactId() + BaseConstants.Symbol.MIDDLE_LINE + getVersion() + "." + artifactType);
        }
        return downloadUrl;
    }

    private String appendWithSlash(String source, String str) {
        if (source.endsWith("/")) {
            source = source.substring(0, source.length() - 1);
        }
        if (str.startsWith("/")) {
            str = str.substring(1, str.length());
        }
        return source + BaseConstants.Symbol.SLASH + str;
    }

    public String getMavenRepoUrl() {
        return mavenRepoUrl;
    }

    public void setMavenRepoUrl(String mavenRepoUrl) {
        this.mavenRepoUrl = mavenRepoUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGitlabPipelineId() {
        return gitlabPipelineId;
    }

    public void setGitlabPipelineId(Long gitlabPipelineId) {
        this.gitlabPipelineId = gitlabPipelineId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getNexusRepoId() {
        return nexusRepoId;
    }

    public void setNexusRepoId(Long nexusRepoId) {
        this.nexusRepoId = nexusRepoId;
    }

    public String getArtifactType() {
        return artifactType;
    }

    public void setArtifactType(String artifactType) {
        this.artifactType = artifactType;
    }
}
