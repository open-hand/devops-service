package io.choerodon.devops.domain.application.event;


/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/4
 * Time: 14:43
 * Description:
 */
public class ProjectMemberPayload {
    private Long projectId;
    private Long userId;
    private Long roleId;


    public ProjectMemberPayload() {
    }

    /**
     * 项目角色创建事件
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @param roleId    角色ID
     */
    public ProjectMemberPayload(Long projectId, Long userId, Long roleId) {
        this.projectId = projectId;
        this.userId = userId;
        this.roleId = roleId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getRoleId() {
        return roleId;
    }
}
