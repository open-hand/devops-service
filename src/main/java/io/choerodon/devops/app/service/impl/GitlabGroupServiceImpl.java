package io.choerodon.devops.app.service.impl;


import static io.choerodon.devops.infra.constant.GitLabConstants.*;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.eventhandler.payload.GitlabGroupPayload;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.app.service.GitlabGroupService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.GroupDTO;
import io.choerodon.devops.infra.enums.Visibility;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.util.TypeUtil;

@Service
public class GitlabGroupServiceImpl implements GitlabGroupService {
    @Autowired
    private DevopsProjectService devopsProjectService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;

    @Override
    public void createGroups(GitlabGroupPayload gitlabGroupPayload) {
        createGroup(gitlabGroupPayload, ENV_GROUP_SUFFIX);
        createGroup(gitlabGroupPayload, CLUSTER_ENV_GROUP_SUFFIX);
        createGroup(gitlabGroupPayload, APP_SERVICE_SUFFIX);
    }

    @Override
    public void updateGroups(GitlabGroupPayload gitlabGroupPayload) {
        updateGroup(gitlabGroupPayload, ENV_GROUP_SUFFIX);
        updateGroup(gitlabGroupPayload, CLUSTER_ENV_GROUP_SUFFIX);
        updateGroup(gitlabGroupPayload, APP_SERVICE_SUFFIX);
    }

    @Override
    public String renderGroupName(String orgName, String projectName, String groupSuffix) {
        // name: orgName-projectName + suffix
        return String.format(GITLAB_GROUP_NAME_FORMAT, orgName, projectName, groupSuffix);
    }

    @Override
    public String renderGroupPath(String orgCode, String projectCode, String suffix) {
        // path: orgName-projectCode + suffix
        return String.format(GITLAB_GROUP_NAME_FORMAT, orgCode, projectCode, suffix);
    }

    @Override
    public GroupDTO createSiteAppGroup(Long iamUserId, String groupName) {
        GroupDTO group = new GroupDTO();
        group.setName(groupName);
        group.setPath(groupName);
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(iamUserId);
        GroupDTO groupDTO = gitlabServiceClientOperator.queryGroupByName(group.getPath(), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        if (groupDTO == null) {
            group.setVisibility(Visibility.PUBLIC);
            groupDTO = gitlabServiceClientOperator.createGroup(group, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }
        return groupDTO;
    }

    private void createGroup(GitlabGroupPayload gitlabGroupPayload, final String suffix) {
        GroupDTO group = new GroupDTO();

        // name: orgName-projectName + suffix
        String name = renderGroupName(gitlabGroupPayload.getOrganizationName(),
                gitlabGroupPayload.getProjectName(), suffix);
        // path: orgName-projectCode + suffix
        String path = renderGroupPath(gitlabGroupPayload.getOrganizationCode(),
                gitlabGroupPayload.getProjectCode(), suffix);

        group.setName(name);
        group.setPath(path);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(gitlabGroupPayload.getUserId());
        GroupDTO groupDTO = gitlabServiceClientOperator.queryGroupByName(group.getPath(), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        if (groupDTO == null) {
            groupDTO = gitlabServiceClientOperator.createGroup(group, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }

        DevopsProjectDTO devopsProjectDO = new DevopsProjectDTO(gitlabGroupPayload.getProjectId());
        setCertainGroupIdBySuffix(suffix, TypeUtil.objToLong(groupDTO.getId()), devopsProjectDO);
        devopsProjectService.baseUpdate(devopsProjectDO);
    }

    /**
     * 更新组
     *
     * @param gitlabGroupPayload 项目信息
     * @param suffix             组名后缀
     */
    private void updateGroup(GitlabGroupPayload gitlabGroupPayload, final String suffix) {
        GroupDTO group = new GroupDTO();

        // name: orgName-projectName + suffix
        String name = renderGroupName(gitlabGroupPayload.getOrganizationName(),
                gitlabGroupPayload.getProjectName(), suffix);
        // path: orgName-projectCode + suffix
        String path = renderGroupPath(gitlabGroupPayload.getOrganizationCode(),
                gitlabGroupPayload.getProjectCode(), suffix);
        group.setName(name);
        group.setPath(path);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(gitlabGroupPayload.getUserId());
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(gitlabGroupPayload.getProjectId());

        Integer groupId = getCertainGroupIdBySuffix(suffix, devopsProjectDTO);

        try {
            gitlabServiceClientOperator.updateGroup(groupId, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), group);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    /**
     * 根据suffix值的不同将groupId设置在不同的字段
     *
     * @param suffix           组 后缀
     * @param groupId          GitLab组id
     * @param devopsProjectDTO project
     */
    private void setCertainGroupIdBySuffix(String suffix, Long groupId, DevopsProjectDTO devopsProjectDTO) {
        switch (suffix) {
            case APP_SERVICE_SUFFIX:
                devopsProjectDTO.setDevopsAppGroupId(groupId);
            case ENV_GROUP_SUFFIX:
                devopsProjectDTO.setDevopsEnvGroupId(groupId);
            case CLUSTER_ENV_GROUP_SUFFIX:
                devopsProjectDTO.setDevopsClusterEnvGroupId(groupId);
            default:
                break;
        }
    }

    /**
     * 根据suffix值获取groupId
     *
     * @param suffix           组后缀
     * @param devopsProjectDTO project
     * @return GitLab组id
     */
    private Integer getCertainGroupIdBySuffix(String suffix, DevopsProjectDTO devopsProjectDTO) {
        switch (suffix) {
            case APP_SERVICE_SUFFIX:
                return TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId());
            case ENV_GROUP_SUFFIX:
                return TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId());
            case CLUSTER_ENV_GROUP_SUFFIX:
                return TypeUtil.objToInteger(devopsProjectDTO.getDevopsClusterEnvGroupId());
            default:
                return null;
        }
    }
}
