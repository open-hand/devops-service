package io.choerodon.devops.infra.dataobject;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * @author younger
 * @date 2018/3/28
 */
@VersionAudit
@ModifyAudit
@Table(name = "devops_application")
public class ApplicationDO extends AuditDomain {

    @Id
    @GeneratedValue
    private Long id;
    private Long projectId;
    private String name;
    private String code;
    private Integer gitlabProjectId;
    private Long appTemplateId;
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

    @Transient
    private String publishLevel;
    @Transient
    private String contributor;
    @Transient
    private String description;

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

    public void setGitlabProjectId(Integer gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }

    public Long getAppTemplateId() {
        return appTemplateId;
    }

    public void setAppTemplateId(Long appTemplateId) {
        this.appTemplateId = appTemplateId;
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
}
