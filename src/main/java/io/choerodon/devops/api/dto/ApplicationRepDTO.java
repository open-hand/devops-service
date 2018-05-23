package io.choerodon.devops.api.dto;

/**
 * Created by younger on 2018/3/30.
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
}
