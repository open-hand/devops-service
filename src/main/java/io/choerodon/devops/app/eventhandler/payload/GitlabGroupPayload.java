package io.choerodon.devops.app.eventhandler.payload;

import java.util.Set;

import io.swagger.annotations.ApiModelProperty;

/**
 * GitLab group create event
 */
public class GitlabGroupPayload {

    private Long projectId;
    private String projectCode;
    @ApiModelProperty("devops组件编码")
    private String devopsComponentCode;
    private String projectName;
    private String organizationCode;
    private String organizationName;
    private String userName;
    private Long userId;
    private Set<String> roleLabels;

    public GitlabGroupPayload() {
    }

    public GitlabGroupPayload(String projectCode, String projectName, String organizationCode, String organizationName, String userName, Long userId) {
        this.projectCode = projectCode;
        this.projectName = projectName;
        this.organizationCode = organizationCode;
        this.organizationName = organizationName;
        this.userName = userName;
        this.userId = userId;
    }

    public String getDevopsComponentCode() {
        return devopsComponentCode;
    }

    public void setDevopsComponentCode(String devopsComponentCode) {
        this.devopsComponentCode = devopsComponentCode;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Set<String> getRoleLabels() {
        return roleLabels;
    }

    public void setRoleLabels(Set<String> roleLabels) {
        this.roleLabels = roleLabels;
    }
}
