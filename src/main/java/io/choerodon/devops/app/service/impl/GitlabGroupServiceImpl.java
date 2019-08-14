package io.choerodon.devops.app.service.impl;


import feign.FeignException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public void createApplicationGroup(ApplicationEventPayload applicationEventPayload) {
        GroupDTO group = new GroupDTO();
        setAppGroupNameAndPath(group, applicationEventPayload);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(applicationEventPayload.getUserId());
        GroupDTO groupDTO = gitlabServiceClientOperator.queryGroupByName(group.getPath(), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        if (groupDTO == null) {
            groupDTO = gitlabServiceClientOperator.createGroup(group, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }

        DevopsProjectDTO devopsProjectDO = new DevopsProjectDTO(applicationEventPayload.getProjectId());
        devopsProjectDO.setDevopsAppGroupId(TypeUtil.objToLong(groupDTO.getId()));
        devopsProjectDO.setAppId(applicationEventPayload.getId());
        devopsProjectService.baseUpdate(devopsProjectDO);
    }


    @Override
    public void updateApplicationGroup(ApplicationEventPayload applicationEventPayload) {
        GroupDTO group = new GroupDTO();
        setAppGroupNameAndPath(group, applicationEventPayload);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(applicationEventPayload.getUserId());
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.queryByAppId(applicationEventPayload.getId());

        Integer groupId = TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId());
        try {
            gitlabServiceClientOperator.updateGroup(groupId, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), group);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }


    private void setAppGroupNameAndPath(GroupDTO group, ApplicationEventPayload applicationEventPayload) {
        String name, path;

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

    @Override
    public void createEnvGroup(GitlabGroupPayload gitlabGroupPayload) {
        GroupDTO group = new GroupDTO();

        // name: orgName-projectName-gitops
        String name = String.format(GROUP_NAME_FORMAT, gitlabGroupPayload.getOrganizationName(),
                gitlabGroupPayload.getProjectName(), ENV_GROUP_SUFFIX);
        // path: orgName-projectCode-gitops
        String path = String.format(GROUP_NAME_FORMAT, gitlabGroupPayload.getOrganizationCode(),
                gitlabGroupPayload.getProjectCode(), ENV_GROUP_SUFFIX);

        group.setName(name);
        group.setPath(path);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(gitlabGroupPayload.getUserId());
        GroupDTO groupDTO = gitlabServiceClientOperator.queryGroupByName(group.getPath(), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        if (groupDTO == null) {
            groupDTO = gitlabServiceClientOperator.createGroup(group, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }

        DevopsProjectDTO devopsProjectDO = new DevopsProjectDTO(gitlabGroupPayload.getProjectId());
        devopsProjectDO.setDevopsEnvGroupId(TypeUtil.objToLong(groupDTO.getId()));
        devopsProjectDO.setAppId(gitlabGroupPayload.getApplicationId());
        devopsProjectService.baseUpdate(devopsProjectDO);
    }

    @Override
    public void updateEnvGroup(GitlabGroupPayload gitlabGroupPayload) {
        GroupDTO group = new GroupDTO();
        // name: orgName-projectName-gitops
        String name = String.format(GROUP_NAME_FORMAT, gitlabGroupPayload.getOrganizationName(),
                gitlabGroupPayload.getProjectName(), ENV_GROUP_SUFFIX);
        // path: orgName-projectCode-gitops
        String path = String.format(GROUP_NAME_FORMAT, gitlabGroupPayload.getOrganizationCode(),
                gitlabGroupPayload.getProjectCode(), ENV_GROUP_SUFFIX);
        group.setName(name);
        group.setPath(path);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(gitlabGroupPayload.getUserId());
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(gitlabGroupPayload.getProjectId());

        Integer groupId = TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId());
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
