package io.choerodon.devops.app.service;

import io.choerodon.devops.app.eventhandler.payload.ProjectPayload;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/2
 * Time: 10:59
 * Description:
 */
public interface ProjectService {
    /**
     * 项目创建事件处理。
     * 创建与 GitLab 相关的数据
     *
     * @param projectPayload 项目创建事件
     */
    void createProject(ProjectPayload projectPayload);

    /**
     * 查询项目在gitlab中组是否创建
     * @param projectId 项目Id
     * @return  gitlab group Ready
     */
    boolean queryProjectGitlabGroupReady(Long projectId);

}
