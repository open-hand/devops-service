package io.choerodon.devops.api.vo.appversion;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author hao.wang@zknow.com
 * @since 2022-07-14 10:42:26
 */
public class AppServiceMavenVersionVO {

    @Encrypt
    private Long id;
    @ApiModelProperty(value = "应用服务版本，devops_app_service_version.id", required = true)
    @Encrypt
    private Long appServiceVersionId;
    @ApiModelProperty(value = "groupId", required = true)
    private String groupId;
    @ApiModelProperty(value = "artifactId", required = true)
    private String artifactId;
    @ApiModelProperty(value = "版本", required = true)
    private String version;
    @ApiModelProperty(value = "nexus仓库id,hrds_prod_repo.rdupm_nexus_repository.repository_id")
    @Encrypt
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
