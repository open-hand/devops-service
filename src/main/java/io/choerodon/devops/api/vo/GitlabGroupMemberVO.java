package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Zenger on 2018/3/28.
 */
public class GitlabGroupMemberVO {

    /**
     * 被更改角色的用户的用户名
     */
    private String username;

    /**
     * 项目Id
     */
    private Long resourceId;

    /**
     * 层级  site/organization/project
     */
    private String resourceType;

    /**
     * 权限列表
     */
    private List<String> roleLabels;

    @ApiModelProperty("用户更新之前的角色标签/目前是组织层有传这个数据,2020-11-19")
    private List<String> previousRoleLabels;

    /**
     * 被更改角色的用户的id
     */
    private Long userId;

    private String uuid;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public List<String> getRoleLabels() {
        return roleLabels;
    }

    public void setRoleLabels(List<String> roleLabels) {
        this.roleLabels = roleLabels;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<String> getPreviousRoleLabels() {
        return previousRoleLabels;
    }

    public void setPreviousRoleLabels(List<String> previousRoleLabels) {
        this.previousRoleLabels = previousRoleLabels;
    }
}
