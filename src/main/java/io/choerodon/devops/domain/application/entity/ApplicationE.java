package io.choerodon.devops.domain.application.entity;

import java.util.Date;

import io.choerodon.devops.domain.application.entity.gitlab.GitlabProjectE;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * Created by younger on 2018/3/28.
 */
@Component
@Scope("prototype")
public class ApplicationE {
    private Long id;
    private ProjectE projectE;
    private String name;
    private String code;
    private GitlabProjectE gitlabProjectE;
    private ApplicationTemplateE applicationTemplateE;
    private DevopsProjectConfigE harborConfigE;
    private DevopsProjectConfigE chartConfigE;
    private Boolean isActive;
    private Boolean isSynchro;
    private String groupName;
    private String uuid;
    private String token;
    private String publishLevel;
    private String contributor;
    private String description;
    private Date lastUpdateDate;
    private String sonarUrl;
    private Long hookId;
    private Boolean isFailed;
    private String type;
    private Boolean isSkipCheckPermission;
    private Long objectVersionNumber;

    public ApplicationE() {
    }

    public ApplicationE(Long id) {
        this.id = id;
    }

    public ApplicationE(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * 构造函数
     *
     * @param id     应用Id
     * @param code   应用code
     * @param name   应用name
     * @param active 是否启用
     */
    public ApplicationE(Long id, String code, String name, Boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.isActive = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProjectE getProjectE() {
        return projectE;
    }

    public void setProjectE(ProjectE projectE) {
        this.projectE = projectE;
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

    public Boolean getSynchro() {
        return isSynchro;
    }

    public void setSynchro(Boolean synchro) {
        isSynchro = synchro;
    }

    public GitlabProjectE getGitlabProjectE() {
        return gitlabProjectE;
    }

    public void setGitlabProjectE(GitlabProjectE gitlabProjectE) {
        this.gitlabProjectE = gitlabProjectE;
    }

    public ApplicationTemplateE getApplicationTemplateE() {
        return applicationTemplateE;
    }

    public void setApplicationTemplateE(ApplicationTemplateE applicationTemplateE) {
        this.applicationTemplateE = applicationTemplateE;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void initProjectE(Long id) {
        this.projectE = new ProjectE(id);
    }

    public void initApplicationTemplateE(Long id) {
        this.applicationTemplateE = new ApplicationTemplateE(id);
    }

    public void initActive(boolean active) {
        this.isActive = active;
    }

    public void initGitlabProjectEByUrl(String url) {
        this.gitlabProjectE.setRepoURL(url);
    }

    public void initGitlabProjectE(Integer id) {
        this.gitlabProjectE = new GitlabProjectE(id);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void initUuid(String uuid) {
        this.uuid = uuid;
    }

    public void initSynchro(boolean synchro) {
        this.isSynchro = synchro;
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

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getSonarUrl() {
        return sonarUrl;
    }

    public void initSonarUrl(String sonarUrl) {
        this.sonarUrl = sonarUrl;
    }

    public Long getHookId() {
        return hookId;
    }

    public void initHookId(Long hookId) {
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

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public void initHarborConfig(Long harborConfigId) {
        this.harborConfigE = new DevopsProjectConfigE(harborConfigId);
    }

    public void initChartConfig(Long chartConfigId) {
        this.chartConfigE =  new DevopsProjectConfigE(chartConfigId);
    }

    public DevopsProjectConfigE getHarborConfigE() {
        return harborConfigE;
    }

    public DevopsProjectConfigE getChartConfigE() {
        return chartConfigE;
    }
}
