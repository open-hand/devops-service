package io.choerodon.devops.api.dto;

/**
 * @author younger
 * @date 2018/3/30
 */
public class ApplicationRepDTO {

    private Long id;
    private String name;
    private String code;
    private Long projectId;
    private Long applictionTemplateId;
    private String repoUrl;
    private Boolean isSynchro;
    private Boolean isActive;
    private String publishLevel;
    private String contributor;
    private String description;
    private String sonarUrl;
    private Boolean isFail;
    private String type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getApplictionTemplateId() {
        return applictionTemplateId;
    }

    public void setApplictionTemplateId(Long applictionTemplateId) {
        this.applictionTemplateId = applictionTemplateId;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public Boolean getSynchro() {
        return isSynchro;
    }

    public void setSynchro(Boolean synchro) {
        isSynchro = synchro;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
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


    public String getSonarUrl() {
        return sonarUrl;
    }

    public void setSonarUrl(String sonarUrl) {
        this.sonarUrl = sonarUrl;
    }

    public Boolean getFail() {
        return isFail;
    }

    public void setFail(Boolean fail) {
        isFail = fail;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
