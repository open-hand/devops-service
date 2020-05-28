package io.choerodon.devops.app.service;

import io.choerodon.devops.app.eventhandler.payload.GitlabGroupPayload;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.GroupDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;

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
     * create cluster env group
     *
     * @param projectDTO      项目
     * @param organizationDTO 组织
     * @param userAttrDTO     当前用户
     */
    void createClusterEnvGroup(ProjectDTO projectDTO, Tenant organizationDTO, UserAttrDTO userAttrDTO);

    /**
     * 创建平台的应用组
     *
     * @return 创建后的组
     */
    GroupDTO createSiteAppGroup(Long iamUserId, String groupName);

}
