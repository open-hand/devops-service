package io.choerodon.devops.app.eventhandler.payload;

import java.util.List;
import java.util.Set;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.api.vo.ProjectCategoryVO;

/**
 * project　event
 *
 * @author crock
 */
public class ProjectPayload {
    private Long projectId;
    private String projectCode;
    private String projectName;
    private String organizationCode;
    private String organizationName;
    @ApiModelProperty("devops组件编码")
    private String devopsComponentCode;
    private String userName;
    private Long userId;
    private String imageUrl;
    private Long programId;
    private Long applicationId;

    private Set<String> roleLabels;
    /**
     * 项目类型的集合
     */
    private List<ProjectCategoryVO> projectCategoryVOS;


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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<ProjectCategoryVO> getProjectCategoryVOS() {
        return projectCategoryVOS;
    }

    public void setProjectCategoryVOS(List<ProjectCategoryVO> projectCategoryVOS) {
        this.projectCategoryVOS = projectCategoryVOS;
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
}
