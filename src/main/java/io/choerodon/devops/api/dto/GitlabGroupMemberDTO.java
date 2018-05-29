package io.choerodon.devops.api.dto;

import java.util.List;

/**
 * Created by Zenger on 2018/3/28.
 */
public class GitlabGroupMemberDTO {

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
}
