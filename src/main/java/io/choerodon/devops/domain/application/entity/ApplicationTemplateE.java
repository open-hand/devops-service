package io.choerodon.devops.domain.application.entity;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.choerodon.devops.domain.application.entity.gitlab.GitlabProjectE;
import io.choerodon.devops.domain.application.valueobject.Organization;

/**
 * Created by younger on 2018/3/27.
 */
@Component
@Scope("prototype")
public class ApplicationTemplateE {
    private Long id;
    private Organization organization;
    private String name;
    private String description;
    private GitlabProjectE gitlabProjectE;
    private String code;
    private Long copyFrom;
    private String repoUrl;
    private Boolean type;
    private String uuid;
    private Boolean isSynchro;
    private Boolean isFailed;


    public ApplicationTemplateE() {

    }

    public ApplicationTemplateE(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getCopyFrom() {
        return copyFrom;
    }

    public void setCopyFrom(Long copyFrom) {
        this.copyFrom = copyFrom;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public void initOrganization(Long id) {
        this.organization = new Organization(id);
    }

    public Boolean getType() {
        return type;
    }

    public void setType(Boolean type) {
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void initUuid(String uuid) {
        this.uuid = uuid;
    }

    public GitlabProjectE getGitlabProjectE() {
        return gitlabProjectE;
    }

    public void initGitlabProjectE(Integer id) {
        this.gitlabProjectE = new GitlabProjectE(id);
    }

    public Boolean getSynchro() {
        return isSynchro;
    }

    public void setSynchro(Boolean synchro) {
        isSynchro = synchro;
    }

    public Boolean getFailed() {
        return isFailed;
    }

    public void setFailed(Boolean failed) {
        isFailed = failed;
    }
}
