package io.choerodon.devops.app.service;

import io.choerodon.devops.app.eventhandler.payload.GitlabGroupPayload;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.GroupDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;

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
     * 通过组织name和项目name获取gitlab项目组的name
     *
     * @param orgName     组织name
     * @param projectName 项目name
     * @param groupSuffix 组后缀,参考 {@link io.choerodon.devops.infra.constant.GitLabConstants}
     * @return group name
     */
    String renderGroupName(String orgName, String projectName, String groupSuffix);

    /**
     * 通过组织code和项目code获取gitlab项目组的path
     *
     * @param orgCode     组织code
     * @param projectCode 项目code
     * @param groupSuffix 组后缀,参考 {@link io.choerodon.devops.infra.constant.GitLabConstants}
     * @return path
     */
    String renderGroupPath(String orgCode, String projectCode, String groupSuffix);

    /**
     * create cluster env group
     *
     * @param projectDTO      项目
     * @param organizationDTO 组织
     * @param userAttrDTO     当前用户
     */
    void createClusterEnvGroup(ProjectDTO projectDTO, OrganizationDTO organizationDTO, UserAttrDTO userAttrDTO);

    /**
     * 创建平台的应用组
     *
     * @return 创建后的组
     */
    GroupDTO createSiteAppGroup(Long iamUserId, String groupName);

}
