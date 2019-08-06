package io.choerodon.devops.app.service.impl;


import java.util.List;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.eventhandler.payload.GitlabGroupPayload;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.app.service.GitlabGroupService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.GroupDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/8
 * Time: 10:05
 * Description:
 */
@Component
public class GitlabGroupServiceImpl implements GitlabGroupService {

    private static final String GITLAB_GROUP_NAME_PATTERN_STRING = "[^\\u4E00-\\u9FA5a-zA-Z0-9_\\-.\\s]";
    private static final String GROUP_NAME_FORMAT = "%s-%s%s";
    private static final String GROUP_APP_MARKET = "application-marker";

    @Autowired
    private DevopsProjectService devopsProjectService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;

    @Override
    public void createGroup(GitlabGroupPayload gitlabGroupPayload, String groupCodeSuffix) {

        String gitlabProjectName = getGitlabProjectName(gitlabGroupPayload);

        //创建gitlab group
        GroupDTO group = new GroupDTO();
        // name: orgName-projectName
        group.setName(String.format(GROUP_NAME_FORMAT,
                gitlabGroupPayload.getOrganizationName(),
                gitlabProjectName,
                groupCodeSuffix));
        // path: orgCode-projectCode
        group.setPath(String.format(GROUP_NAME_FORMAT,
                gitlabGroupPayload.getOrganizationCode(),
                gitlabGroupPayload.getProjectCode(),
                groupCodeSuffix));
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(gitlabGroupPayload.getUserId());
        GroupDTO groupDTO = gitlabServiceClientOperator.queryGroupByName(group.getPath(), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        if (groupDTO == null) {
            groupDTO = gitlabServiceClientOperator.createGroup(group, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }
        DevopsProjectDTO devopsProjectDO = new DevopsProjectDTO(gitlabGroupPayload.getProjectId());
        if (groupCodeSuffix.isEmpty()) {
            devopsProjectDO.setDevopsAppGroupId(TypeUtil.objToLong(groupDTO.getId()));
        } else if ("-gitops".equals(groupCodeSuffix)) {
            devopsProjectDO.setDevopsEnvGroupId(TypeUtil.objToLong(groupDTO.getId()));
        }
        devopsProjectService.baseUpdate(devopsProjectDO);
    }

    @Override
    public GroupDTO createAppMarketGroup(GitlabGroupPayload gitlabGroupPayload) {
        //创建gitlab group
        GroupDTO group = new GroupDTO();
        // name: orgName-application-market
        group.setName(String.format("%s-%s",
                gitlabGroupPayload.getOrganizationName(),
                GROUP_APP_MARKET));
        // path: orgCode-application-market
        group.setPath(String.format("%s-%s",
                gitlabGroupPayload.getOrganizationCode(),
                GROUP_APP_MARKET));
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(gitlabGroupPayload.getUserId());
        GroupDTO groupDTO = gitlabServiceClientOperator.queryGroupByName(group.getPath(), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        if (groupDTO == null) {
            groupDTO = gitlabServiceClientOperator.createGroup(group, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }
        return groupDTO;
    }

    private String getGitlabProjectName(GitlabGroupPayload gitlabGroupPayload) {
        ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(gitlabGroupPayload.getProjectId());
        OrganizationDTO organizationDTO = iamServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        List<ProjectDTO> projectES = iamServiceClientOperator.listIamProjectByOrgId(organizationDTO.getId(), gitlabGroupPayload.getProjectName(), null);
        String validProjectName = getValidGroupName(gitlabGroupPayload.getProjectName());
        return projectES.size() > 1 ? validProjectName + "-" + (projectES.size() - 1) : validProjectName;
    }

    @Override
    public void updateGroup(GitlabGroupPayload gitlabGroupPayload, String groupCodeSuffix) {
        String gitlabProjectName = getGitlabProjectName(gitlabGroupPayload);

        //创建gitlab group
        GroupDTO group = new GroupDTO();
        // name: orgName-projectName
        group.setName(String.format(GROUP_NAME_FORMAT,
                gitlabGroupPayload.getOrganizationName(),
                gitlabProjectName,
                groupCodeSuffix));
        // path: orgCode-projectCode
        group.setPath(String.format(GROUP_NAME_FORMAT,
                gitlabGroupPayload.getOrganizationCode(),
                gitlabGroupPayload.getProjectCode(),
                groupCodeSuffix));

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(gitlabGroupPayload.getUserId());
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(gitlabGroupPayload.getProjectId());

        Integer groupId;
        if (groupCodeSuffix.isEmpty()) {
            groupId = TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId());
        } else {
            groupId = TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId());
        }
        try {
            gitlabServiceClientOperator.updateGroup(groupId, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), group);
        } catch (FeignException e) {
            throw new CommonException(e);
        }

    }

    /**
     * process the original name to get a valid name.
     * The invalid characters will be replaced by '_' (lower dash)
     *
     * @param groupName the original group name
     * @return a valid name after processed
     */
    private String getValidGroupName(String groupName) {
        return groupName == null ? null : groupName.replaceAll(GITLAB_GROUP_NAME_PATTERN_STRING, "_");
    }
}
