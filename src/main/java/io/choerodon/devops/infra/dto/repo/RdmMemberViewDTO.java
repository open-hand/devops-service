package io.choerodon.devops.infra.dto.repo;

import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;


public class RdmMemberViewDTO {
    @ApiModelProperty("用户")
    private BaseC7nUserViewDTO user;

    @ApiModelProperty("项目角色")
    private List<String> roleNames;


    /**
     * 成员id, 主键
     */
    private Long id;

    @ApiModelProperty("项目id")
    private Long projectId;


    @ApiModelProperty("代码库id")
    private Long repositoryId;

    private Long userId;

    @ApiModelProperty("权限")
    private Integer glAccessLevel;

    @ApiModelProperty("过期时间")
    private Date glExpiresAt;

    @ApiModelProperty("Gitlab同步标识")
    private Boolean syncGitlabFlag;

    @ApiModelProperty("Gitlab同步时间")
    private Date syncGitlabDate;

    public BaseC7nUserViewDTO getUser() {
        return user;
    }

    public void setUser(BaseC7nUserViewDTO user) {
        this.user = user;
    }

    public List<String> getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(List<String> roleNames) {
        this.roleNames = roleNames;
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

    public Long getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(Long repositoryId) {
        this.repositoryId = repositoryId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getGlAccessLevel() {
        return glAccessLevel;
    }

    public void setGlAccessLevel(Integer glAccessLevel) {
        this.glAccessLevel = glAccessLevel;
    }

    public Date getGlExpiresAt() {
        return glExpiresAt;
    }

    public void setGlExpiresAt(Date glExpiresAt) {
        this.glExpiresAt = glExpiresAt;
    }

    public Boolean getSyncGitlabFlag() {
        return syncGitlabFlag;
    }

    public void setSyncGitlabFlag(Boolean syncGitlabFlag) {
        this.syncGitlabFlag = syncGitlabFlag;
    }

    public Date getSyncGitlabDate() {
        return syncGitlabDate;
    }

    public void setSyncGitlabDate(Date syncGitlabDate) {
        this.syncGitlabDate = syncGitlabDate;
    }
}

