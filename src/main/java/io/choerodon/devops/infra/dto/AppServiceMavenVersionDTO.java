package io.choerodon.devops.infra.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 应用版本表(AppServiceMavenVersion)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-13 16:47:42
 */

@ApiModel("应用版本表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_app_service_maven_version")
public class AppServiceMavenVersionDTO extends AuditDomain {
    private static final long serialVersionUID = 801548481676256212L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_APP_SERVICE_VERSION_ID = "appServiceVersionId";
    public static final String FIELD_GROUP_ID = "groupId";
    public static final String FIELD_ARTIFACT_ID = "artifactId";
    public static final String FIELD_VERSION = "version";
    public static final String FIELD_NEXUS_REPO_ID = "nexusRepoId";
    public static final String FIELD_MAVEN_REPO_URL = "mavenRepoUrl";
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_PASSWORD = "password";

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "应用服务版本，devops_app_service_version.id", required = true)
    @NotNull
    private Long appServiceVersionId;

    @ApiModelProperty(value = "groupId", required = true)
    @NotBlank
    private String groupId;

    @ApiModelProperty(value = "artifactId", required = true)
    @NotBlank
    private String artifactId;

    @ApiModelProperty(value = "版本", required = true)
    @NotBlank
    private String version;

    @ApiModelProperty(value = "nexus仓库id,hrds_prod_repo.rdupm_nexus_repository.repository_id")
    private Long nexusRepoId;

    private String mavenRepoUrl;

    private String username;

    private String password;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppServiceVersionId() {
        return appServiceVersionId;
    }

    public void setAppServiceVersionId(Long appServiceVersionId) {
        this.appServiceVersionId = appServiceVersionId;
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

}

