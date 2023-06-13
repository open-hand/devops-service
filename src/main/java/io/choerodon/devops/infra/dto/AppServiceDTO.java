package io.choerodon.devops.infra.dto;

import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

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

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("所属项目id")
    private Long projectId;
    @ApiModelProperty("应用服务名称")
    private String name;
    @ApiModelProperty("应用服务编码")
    private String code;
    @ApiModelProperty("关联的gitlab Project id")
    private Integer gitlabProjectId;
    @ApiModelProperty("关联的docker仓库 id")
    private Long harborConfigId;
    @ApiModelProperty("关联的chart仓库 id")
    private Long chartConfigId;
    @ApiModelProperty("是否启用")
    private Boolean isActive;
    @ApiModelProperty("是否同步")
    private Boolean isSynchro;
    @ApiModelProperty("应用服务唯一token")
    private String token;

    @ApiModelProperty("应用服务附加的pom信息：groupId（敏捷使用）")
    private String groupId;
    @ApiModelProperty("应用服务附加的pom信息：artifactId（敏捷使用）")
    private String artifactId;
    @ApiModelProperty("应用服务gitlab webhook id")
    private Long hookId;
    @ApiModelProperty("是否创建失败")
    private Boolean isFailed;
    @ApiModelProperty("应用服务类型，普通、测试")
    private String type;
    @ApiModelProperty("应用服务图标地址")
    private String imgUrl;
    @ApiModelProperty("应用服务创建错误信息")
    private String errorMessage;

    @ApiModelProperty("外部应用服务gitlab地址")
    private String externalGitlabUrl;

    @Encrypt
    @ApiModelProperty("外部仓库配置id")
    private Long externalConfigId;
    @Transient
    private String repoUrl;

    @Transient
    @ApiModelProperty("应用服务对应的gitlab的仓库的ssh协议克隆地址")
    private String sshRepositoryUrl;

    @Transient
    @ApiModelProperty("外置仓库配置信息")
    private AppExternalConfigDTO appExternalConfigDTO;
    @Transient
    private Double codeScore;
    @Transient
    private Double vulnScore;

    public Double getCodeScore() {
        return codeScore;
    }

    public void setCodeScore(Double codeScore) {
        this.codeScore = codeScore;
    }

    public Double getVulnScore() {
        return vulnScore;
    }

    public void setVulnScore(Double vulnScore) {
        this.vulnScore = vulnScore;
    }

    public AppExternalConfigDTO getAppExternalConfigDTO() {
        return appExternalConfigDTO;
    }

    public void setAppExternalConfigDTO(AppExternalConfigDTO appExternalConfigDTO) {
        this.appExternalConfigDTO = appExternalConfigDTO;
    }

    public Long getExternalConfigId() {
        return externalConfigId;
    }

    public void setExternalConfigId(Long externalConfigId) {
        this.externalConfigId = externalConfigId;
    }

    public String getExternalGitlabUrl() {
        return externalGitlabUrl;
    }

    public void setExternalGitlabUrl(String externalGitlabUrl) {
        this.externalGitlabUrl = externalGitlabUrl;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
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
                ", token='" + token + '\'' +
                ", hookId=" + hookId +
                ", isFailed=" + isFailed +
                ", type='" + type + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", repoUrl='" + repoUrl + '\'' +
                ", sshRepositoryUrl='" + sshRepositoryUrl + '\'' +
                '}';
    }
}
