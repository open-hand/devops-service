package io.choerodon.devops.infra.dto;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import javax.persistence.*;

/**
 * if (!isSynchro) {
 *     // 处理中
 * } else {
 *     if (isFailed) {
 *         // 失败
 *     } else {
 *         // 成功
 *     }
 * }
 * @author younger
 * @date 2018/3/28
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_app_service")
public class AppServiceDTO extends AuditDomain {

    public static final String ENCRYPT_KEY = "devops_app_service";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Encrypt(AppServiceDTO.ENCRYPT_KEY)
    private Long id;
    private Long projectId;
    private String name;
    private String code;
    private Integer gitlabProjectId;
    private Long harborConfigId;
    private Long chartConfigId;
    private Boolean isActive;
    private Boolean isSynchro;
    private String uuid;
    private String token;
    private Long hookId;
    private Boolean isFailed;
    private String type;
    private Boolean isSkipCheckPermission;
    private String imgUrl;
    // TODO delete the field
    @Deprecated
    private Long mktAppId;

    @Transient
    private String publishLevel;
    @Transient
    private String contributor;
    @Transient
    private String description;
    @Transient
    private String repoUrl;

    @Transient
    @ApiModelProperty("应用服务对应的gitlab的仓库的ssh协议克隆地址")
    private String sshRepositoryUrl;

    @Transient
    private String sonarUrl;
    @Transient
    private String gitlabProjectUrl;
    @Transient
    private String version;

    /**
     * 是否是空仓库(是否没有分支)
     */
    @Transient
    private Boolean emptyRepository;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getGitlabProjectUrl() {
        return gitlabProjectUrl;
    }

    public void setGitlabProjectUrl(String gitlabProjectUrl) {
        this.gitlabProjectUrl = gitlabProjectUrl;
    }

    public String getSonarUrl() {
        return sonarUrl;
    }

    public void setSonarUrl(String sonarUrl) {
        this.sonarUrl = sonarUrl;
    }

    public Boolean getSkipCheckPermission() {
        return isSkipCheckPermission;
    }

    public void setSkipCheckPermission(Boolean skipCheckPermission) {
        isSkipCheckPermission = skipCheckPermission;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getGitlabProjectId() {
        return gitlabProjectId;
    }

    public AppServiceDTO setGitlabProjectId(Integer gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
        return this;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Boolean getSynchro() {
        return isSynchro;
    }

    public void setSynchro(Boolean synchro) {
        isSynchro = synchro;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPublishLevel() {
        return publishLevel;
    }

    public void setPublishLevel(String publishLevel) {
        this.publishLevel = publishLevel;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getHookId() {
        return hookId;
    }

    public void setHookId(Long hookId) {
        this.hookId = hookId;
    }

    public Boolean getFailed() {
        return isFailed;
    }

    public void setFailed(Boolean failed) {
        isFailed = failed;
    }

    public Boolean getIsSkipCheckPermission() {
        return isSkipCheckPermission;
    }

    public void setIsSkipCheckPermission(Boolean skipCheckPermission) {
        isSkipCheckPermission = skipCheckPermission;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getHarborConfigId() {
        return harborConfigId;
    }

    public void setHarborConfigId(Long harborConfigId) {
        this.harborConfigId = harborConfigId;
    }

    public Long getChartConfigId() {
        return chartConfigId;
    }

    public void setChartConfigId(Long chartConfigId) {
        this.chartConfigId = chartConfigId;
    }

    public Long getMktAppId() {
        return mktAppId;
    }

    public void setMktAppId(Long mktAppId) {
        this.mktAppId = mktAppId;
    }

    public Boolean getEmptyRepository() {
        return emptyRepository;
    }

    public void setEmptyRepository(Boolean emptyRepository) {
        this.emptyRepository = emptyRepository;
    }

    public String getSshRepositoryUrl() {
        return sshRepositoryUrl;
    }

    public void setSshRepositoryUrl(String sshRepositoryUrl) {
        this.sshRepositoryUrl = sshRepositoryUrl;
    }

    @Override
    public String toString() {
        return "AppServiceDTO{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", gitlabProjectId=" + gitlabProjectId +
                ", harborConfigId=" + harborConfigId +
                ", chartConfigId=" + chartConfigId +
                ", isActive=" + isActive +
                ", isSynchro=" + isSynchro +
                ", uuid='" + uuid + '\'' +
                ", token='" + token + '\'' +
                ", hookId=" + hookId +
                ", isFailed=" + isFailed +
                ", type='" + type + '\'' +
                ", isSkipCheckPermission=" + isSkipCheckPermission +
                ", imgUrl='" + imgUrl + '\'' +
                ", mktAppId=" + mktAppId +
                ", publishLevel='" + publishLevel + '\'' +
                ", contributor='" + contributor + '\'' +
                ", description='" + description + '\'' +
                ", repoUrl='" + repoUrl + '\'' +
                ", sshRepositoryUrl='" + sshRepositoryUrl + '\'' +
                ", sonarUrl='" + sonarUrl + '\'' +
                ", gitlabProjectUrl='" + gitlabProjectUrl + '\'' +
                ", version='" + version + '\'' +
                ", emptyRepository=" + emptyRepository +
                '}';
    }
}
