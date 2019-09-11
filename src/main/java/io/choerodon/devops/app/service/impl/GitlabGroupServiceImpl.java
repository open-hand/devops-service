package io.choerodon.devops.app.service.impl;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.eventhandler.payload.ApplicationEventPayload;
import io.choerodon.devops.app.eventhandler.payload.GitlabGroupPayload;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.app.service.GitlabGroupService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.GroupDTO;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/8
 * Time: 10:05
 * Description:
 */
@Service
public class GitlabGroupServiceImpl implements GitlabGroupService {
    private static final String GROUP_NAME_FORMAT = "%s-%s%s";
    private static final String SITE_APP_GROUP_NAME_FORMAT = "site_%s";
    private static final String GROUP_APP_MARKET = "application-marker";
    private static final String ENV_GROUP_SUFFIX = "-gitops";

    @Autowired
    private DevopsProjectService devopsProjectService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;

    @Override
    public void createGroups(GitlabGroupPayload gitlabGroupPayload) {
        createGroup(gitlabGroupPayload, ENV_GROUP_SUFFIX);
        createGroup(gitlabGroupPayload, null);
    }

    @Override
    public void updateGroups(GitlabGroupPayload gitlabGroupPayload) {
        updateGroup(gitlabGroupPayload, ENV_GROUP_SUFFIX);
        updateGroup(gitlabGroupPayload, null);
    }

    public void createApplicationGroup(ApplicationEventPayload applicationEventPayload) {
        GroupDTO group = new GroupDTO();
        setAppGroupNameAndPath(group, applicationEventPayload);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(applicationEventPayload.getUserId());
        GroupDTO groupDTO = gitlabServiceClientOperator.queryGroupByName(group.getPath(), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        if (groupDTO == null) {
            group.setVisibility(applicationEventPayload.getVisibility());
            groupDTO = gitlabServiceClientOperator.createGroup(group, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }

        DevopsProjectDTO devopsProjectDO = new DevopsProjectDTO(applicationEventPayload.getProjectId());
        devopsProjectDO.setDevopsAppGroupId(TypeUtil.objToLong(groupDTO.getId()));
        devopsProjectService.baseUpdate(devopsProjectDO);
    }

    @Override
    public GroupDTO createSiteAppGroup() {
        // TODO by scp
        return null;
    }

    @Nonnull
    @Override
    public GroupDTO querySiteAppGroup() {
        // TODO by scp
        GroupDTO group = null;

        if (group == null) {
            return createSiteAppGroup();
        }
        return group;
    }


    private void setAppGroupNameAndPath(GroupDTO group, ApplicationEventPayload applicationEventPayload) {
        String name;
        String path;

        if (applicationEventPayload.getProjectId() != null) {
            // 项目下应用
            // name: orgName-appName
            name = String.format(GROUP_NAME_FORMAT, applicationEventPayload.getOrganizationName(),
                    applicationEventPayload.getName(), "");
            // path: orgName-appCode
            path = String.format(GROUP_NAME_FORMAT, applicationEventPayload.getOrganizationCode(),
                    applicationEventPayload.getCode(), "");
        } else {
            // 平台下应用
            // name: site-appName
            name = String.format(SITE_APP_GROUP_NAME_FORMAT, applicationEventPayload.getName());
            // path: site-appCode
            path = String.format(SITE_APP_GROUP_NAME_FORMAT, applicationEventPayload.getCode());
        }


        group.setName(name);
        group.setPath(path);
    }

    private void createGroup(GitlabGroupPayload gitlabGroupPayload, @Nullable final String suffix) {
        final String actualSuffix = suffix == null ? "" : suffix;

        GroupDTO group = new GroupDTO();

        // name: orgName-projectName + suffix
        String name = String.format(GROUP_NAME_FORMAT, gitlabGroupPayload.getOrganizationName(),
                gitlabGroupPayload.getProjectName(), actualSuffix);
        // path: orgName-projectCode + suffix
        String path = String.format(GROUP_NAME_FORMAT, gitlabGroupPayload.getOrganizationCode(),
                gitlabGroupPayload.getProjectCode(), actualSuffix);

        group.setName(name);
        group.setPath(path);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(gitlabGroupPayload.getUserId());
        GroupDTO groupDTO = gitlabServiceClientOperator.queryGroupByName(group.getPath(), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        if (groupDTO == null) {
            groupDTO = gitlabServiceClientOperator.createGroup(group, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }

        DevopsProjectDTO devopsProjectDO = new DevopsProjectDTO(gitlabGroupPayload.getProjectId());
        if (ENV_GROUP_SUFFIX.equals(suffix)) {
            devopsProjectDO.setDevopsEnvGroupId(TypeUtil.objToLong(groupDTO.getId()));
        } else {
            devopsProjectDO.setDevopsAppGroupId(TypeUtil.objToLong(groupDTO.getId()));
        }
        devopsProjectService.baseUpdate(devopsProjectDO);
    }

    /**
     * 更新组
     *
     * @param gitlabGroupPayload 项目信息
     * @param suffix             组名后缀，可为 null
     */
    private void updateGroup(GitlabGroupPayload gitlabGroupPayload, @Nullable final String suffix) {
        final String actualSuffix = suffix == null ? "" : suffix;

        GroupDTO group = new GroupDTO();

        // name: orgName-projectName + suffix
        String name = String.format(GROUP_NAME_FORMAT, gitlabGroupPayload.getOrganizationName(),
                gitlabGroupPayload.getProjectName(), actualSuffix);
        // path: orgName-projectCode + suffix
        String path = String.format(GROUP_NAME_FORMAT, gitlabGroupPayload.getOrganizationCode(),
                gitlabGroupPayload.getProjectCode(), actualSuffix);
        group.setName(name);
        group.setPath(path);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(gitlabGroupPayload.getUserId());
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(gitlabGroupPayload.getProjectId());

        Integer groupId;
        if (ENV_GROUP_SUFFIX.equals(suffix)) {
            groupId = TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId());
        } else {
            groupId = TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId());
        }

        try {
            gitlabServiceClientOperator.updateGroup(groupId, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), group);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
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
}
