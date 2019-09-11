package io.choerodon.devops.app.service;

import javax.annotation.Nonnull;

import io.choerodon.devops.app.eventhandler.payload.ApplicationEventPayload;
import io.choerodon.devops.app.eventhandler.payload.GitlabGroupPayload;
import io.choerodon.devops.infra.dto.gitlab.GroupDTO;

/**
 * GitLab组相关的操作
 */
public interface GitlabGroupService {
    /**
     * 创建对应项目的两个GitLab组
     *
     * @param gitlabGroupPayload 用于创建组的项目信息
     */
    void createGroups(GitlabGroupPayload gitlabGroupPayload);

    /**
     * 更新对应项目的两个GitLab组
     *
     * @param gitlabGroupPayload 项目信息
     */
    void updateGroups(GitlabGroupPayload gitlabGroupPayload);

    /**
     * 创建应用相关的group，包括平台下的应用及项目下应用
     *
     * @param applicationEventPayload 应用信息
     */
    void createApplicationGroup(ApplicationEventPayload applicationEventPayload);

    /**
     * 创建平台的应用组
     *
     * @return 创建后的组
     */
    GroupDTO createSiteAppGroup();

    /**
     * 查询平台的应用组，如果不存在则创建
     *
     * @return 查询到的应用组
     */
    @Nonnull
    GroupDTO querySiteAppGroup();

    /**
     * 为应用下载创建 group
     *
     * @param gitlabGroupPayload
     * @return
     */
    GroupDTO createAppMarketGroup(GitlabGroupPayload gitlabGroupPayload);
}
