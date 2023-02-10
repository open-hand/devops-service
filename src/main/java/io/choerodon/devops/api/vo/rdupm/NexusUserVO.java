package io.choerodon.devops.api.vo.rdupm;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author hao.wang@zknow.com
 * @since 2023-01-10 15:21:15
 */
public class NexusUserVO {

    @ApiModelProperty("表ID，主键，供其他表做外键")
    private Long userId;
    @ApiModelProperty(value = "rdupm_nexus_repository表主键", required = true)
    private Long repositoryId;
    @ApiModelProperty(value = "仓库默认发布用户Id")
    private String neUserId;
    @ApiModelProperty(value = "仓库默认发布用户密码")
    private String neUserPassword;
    @ApiModelProperty(value = "仓库默认拉取用户Id")
    private String nePullUserId;
    @ApiModelProperty(value = "仓库默认拉取用户密码")
    private String nePullUserPassword;
    @ApiModelProperty(value = "租户Id")
    private Long tenantId;
    @ApiModelProperty(value = "默认仓库名称")
    private String neRepositoryName;
    @ApiModelProperty(value = "默认管理用户角色")
    private String neRoleId;
    @ApiModelProperty(value = "组织Id")
    private Long organizationId;
    @ApiModelProperty(value = "项目id")
    private Long projectId;
    @ApiModelProperty(value = "旧密码")
    private String oldNeUserPassword;
    @ApiModelProperty(value = "是否可编辑-项目层")
    private Boolean editFlag;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(Long repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getNeUserId() {
        return neUserId;
    }

    public void setNeUserId(String neUserId) {
        this.neUserId = neUserId;
    }

    public String getNeUserPassword() {
        return neUserPassword;
    }

    public void setNeUserPassword(String neUserPassword) {
        this.neUserPassword = neUserPassword;
    }

    public String getNePullUserId() {
        return nePullUserId;
    }

    public void setNePullUserId(String nePullUserId) {
        this.nePullUserId = nePullUserId;
    }

    public String getNePullUserPassword() {
        return nePullUserPassword;
    }

    public void setNePullUserPassword(String nePullUserPassword) {
        this.nePullUserPassword = nePullUserPassword;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getNeRepositoryName() {
        return neRepositoryName;
    }

    public void setNeRepositoryName(String neRepositoryName) {
        this.neRepositoryName = neRepositoryName;
    }

    public String getNeRoleId() {
        return neRoleId;
    }

    public void setNeRoleId(String neRoleId) {
        this.neRoleId = neRoleId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getOldNeUserPassword() {
        return oldNeUserPassword;
    }

    public void setOldNeUserPassword(String oldNeUserPassword) {
        this.oldNeUserPassword = oldNeUserPassword;
    }

    public Boolean getEditFlag() {
        return editFlag;
    }

    public void setEditFlag(Boolean editFlag) {
        this.editFlag = editFlag;
    }
}
