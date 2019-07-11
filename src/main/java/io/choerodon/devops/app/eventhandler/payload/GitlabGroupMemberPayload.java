package io.choerodon.devops.app.eventhandler.payload;

import java.util.List;

/**
 * 从消息队列拿到的信息对应的实体
 * Created by Zenger on 2018/3/27.
 */
public class GitlabGroupMemberPayload {

    /**
     * 被更改角色的用户的用户名
     */
    private String username;

    /**
     * 项目Id
     */
    private Long projectId;

    private String resourceType;

    /**
     * 权限列表
     */
    private List<String> roleLabels;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public List<String> getRoleLabels() {
        return roleLabels;
    }

    public void setRoleLabels(List<String> roleLabels) {
        this.roleLabels = roleLabels;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
