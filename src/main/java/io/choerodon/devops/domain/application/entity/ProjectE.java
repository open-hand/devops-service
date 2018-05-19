package io.choerodon.devops.domain.application.entity;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE;
import io.choerodon.devops.domain.application.valueobject.Organization;

/**
 * Created by Zenger on 2018/3/28.
 */
@Component
@Scope("prototype")
public class ProjectE {
    private Long id;
    private Organization organization;
    private String name;
    private String code;
    private GitlabGroupE gitlabGroupE;

    public ProjectE() {
    }

    public ProjectE(Long id) {
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public GitlabGroupE getGitlabGroupE() {
        return gitlabGroupE;
    }

    public void setGitlabGroupE(GitlabGroupE gitlabGroupE) {
        this.gitlabGroupE = gitlabGroupE;
    }

    public void initGitlabGroupE(Long id) {
        this.gitlabGroupE = new GitlabGroupE(id.intValue());
    }

    public void initOrganization(Long id) {
        this.organization = new Organization(id);
    }
}
