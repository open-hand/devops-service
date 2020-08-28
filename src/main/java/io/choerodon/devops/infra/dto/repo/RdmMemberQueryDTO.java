package io.choerodon.devops.infra.dto.repo;

import java.util.Set;

import io.swagger.annotations.ApiModelProperty;

public class RdmMemberQueryDTO {
    /**
     * 组织层
     */
    @ApiModelProperty(value = "项目id")
    private Set<Long> projectIds;
    /**
     * 项目层
     */
    @ApiModelProperty(value = "应用服务id")
    private Set<Long> repositoryIds;

    @ApiModelProperty(value = "应用服务名称")
    private String repositoryName;
    @ApiModelProperty(value = "用户名")
    private String realName;
    @ApiModelProperty(value = "登录名")
    private String loginName;
    @ApiModelProperty(value = "通用参数")
    private String params;
    @ApiModelProperty(value = "是否启用")
    private Boolean enabled;
    @ApiModelProperty(value = "是否同步")
    private Boolean syncGitlabFlag;
    @ApiModelProperty(value = "是否过期")
    private Boolean glExpiresFlag;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getSyncGitlabFlag() {
        return syncGitlabFlag;
    }

    public void setSyncGitlabFlag(Boolean syncGitlabFlag) {
        this.syncGitlabFlag = syncGitlabFlag;
    }

    public Boolean getGlExpiresFlag() {
        return glExpiresFlag;
    }

    public void setGlExpiresFlag(Boolean glExpiresFlag) {
        this.glExpiresFlag = glExpiresFlag;
    }

    public Set<Long> getProjectIds() {
        return projectIds;
    }

    public RdmMemberQueryDTO setProjectIds(Set<Long> projectIds) {
        this.projectIds = projectIds;
        return this;
    }

    public Set<Long> getRepositoryIds() {
        return repositoryIds;
    }

    public RdmMemberQueryDTO setRepositoryIds(Set<Long> repositoryIds) {
        this.repositoryIds = repositoryIds;
        return this;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public RdmMemberQueryDTO setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
        return this;
    }

    public String getRealName() {
        return realName;
    }

    public RdmMemberQueryDTO setRealName(String realName) {
        this.realName = realName;
        return this;
    }

    public String getLoginName() {
        return loginName;
    }

    public RdmMemberQueryDTO setLoginName(String loginName) {
        this.loginName = loginName;
        return this;
    }

    public String getParams() {
        return params;
    }

    public RdmMemberQueryDTO setParams(String params) {
        this.params = params;
        return this;
    }
}
