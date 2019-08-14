package io.choerodon.devops.app.service;

import io.choerodon.devops.app.eventhandler.payload.ApplicationEventPayload;
import io.choerodon.devops.app.eventhandler.payload.GitlabGroupPayload;
import io.choerodon.devops.infra.dto.gitlab.GroupDTO;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/8
 * Time: 10:04
 * Description:
 */
public interface GitlabGroupService {
    /**
     * 创建应用相关的group，包括平台下的应用及项目下应用
     *
     * @param applicationEventPayload 应用信息
     */
    void createApplicationGroup(ApplicationEventPayload applicationEventPayload);

    /**
     * 更新应用相关的组
     *
     * @param applicationEventPayload 应用信息
     */
    void updateApplicationGroup(ApplicationEventPayload applicationEventPayload);

    /**
     * 创建环境组
     * @param gitlabGroupPayload 环境组信息
     */
    void createEnvGroup(GitlabGroupPayload gitlabGroupPayload);

    /**
     * 更新环境组
     * @param gitlabGroupPayload 环境组信息
     */
    void updateEnvGroup(GitlabGroupPayload gitlabGroupPayload);

    /**
     * 为应用下载创建 group
     *
     * @param gitlabGroupPayload
     * @return
     */
    GroupDTO createAppMarketGroup(GitlabGroupPayload gitlabGroupPayload);
}
