package io.choerodon.devops.app.eventhandler.payload;

import java.util.Set;

/**
 * project　event
 *
 * @author crock
 */
public class ProjectPayload {
    private Long projectId;
    private String projectCode;
    private String projectName;
    private String projectCategory;
    private String organizationCode;
    private String organizationName;
    private String userName;
    private Long userId;
    private String imageUrl;
    private Long programId;
    private Long applicationId;

    private ApplicationEventPayload applicationEventPayload;

    private Set<String> roleLabels;

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getProjectCategory() {
        return projectCategory;
    }

    public void setProjectCategory(String projectCategory) {
        this.projectCategory = projectCategory;
    }

    public Long getProgramId() {
        return programId;
    }

    public void setProgramId(Long programId) {
        this.programId = programId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public ApplicationEventPayload getApplicationEventPayload() {
        return applicationEventPayload;
    }

    public void setApplicationEventPayload(ApplicationEventPayload applicationEventPayload) {
        this.applicationEventPayload = applicationEventPayload;
    }
}
