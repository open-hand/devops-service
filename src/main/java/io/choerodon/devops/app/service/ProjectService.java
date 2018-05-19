package io.choerodon.devops.app.service;

import io.choerodon.devops.domain.application.event.ProjectEvent;

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
     * @param projectEvent 项目创建事件
     */
    void createProject(ProjectEvent projectEvent);

    Boolean groupExist(String uuid);

    Boolean harborExist(String uuid);

    Boolean memberExist(String uuid);
}
