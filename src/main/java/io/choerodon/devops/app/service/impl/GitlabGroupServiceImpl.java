package io.choerodon.devops.app.service.impl;


import static io.choerodon.devops.infra.constant.GitOpsConstants.*;

import java.util.List;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.OrgAdministratorVO;
import io.choerodon.devops.app.eventhandler.payload.GitlabGroupPayload;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.app.service.GitlabGroupMemberService;
import io.choerodon.devops.app.service.GitlabGroupService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.GroupDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.AccessLevel;
import io.choerodon.devops.infra.enums.Visibility;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.util.GitOpsUtil;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.TypeUtil;

@Service
public class GitlabGroupServiceImpl implements GitlabGroupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabGroupServiceImpl.class);
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsProjectService devopsProjectService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;

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

    /**
     * create cluster env group
     *
     * @param projectDTO      choerodon平台项目
     * @param organizationDTO choerodon平台组织
     * @param userAttrDTO     当前用户
     */
    @Override
    public void createClusterEnvGroup(ProjectDTO projectDTO, Tenant organizationDTO, UserAttrDTO userAttrDTO) {
        GitlabGroupPayload payload = new GitlabGroupPayload();
        payload.setOrganizationCode(organizationDTO.getTenantNum());
        payload.setOrganizationName(organizationDTO.getTenantName());
        payload.setProjectCode(projectDTO.getCode());
        payload.setProjectName(projectDTO.getName());
        payload.setProjectId(projectDTO.getId());
        payload.setUserId(userAttrDTO.getIamUserId());
        createGroup(payload, CLUSTER_ENV_GROUP_SUFFIX);

        List<Long> ownerIds = baseServiceClientOperator.getAllOwnerIds(projectDTO.getId());
        //创建完group后分配组织管理员权限
        List<OrgAdministratorVO> list = baseServiceClientOperator.listOrgAdministrator(projectDTO.getOrganizationId()).getContent();
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(orgAdministratorVO -> gitlabGroupMemberService.assignGitLabGroupMemberForOwner(projectDTO, orgAdministratorVO.getId()));
        }
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectDTO.getId());
        if (devopsProjectDTO.getDevopsClusterEnvGroupId() == null) {
            throw new CommonException("error.cluster.env.group.create");
        }
        ownerIds.forEach(id -> {
                    UserAttrDTO ownerAttrDTO = userAttrService.baseQueryById(id);
                    MemberDTO memberDTO = new MemberDTO(ownerAttrDTO.getGitlabUserId().intValue(), AccessLevel.OWNER.value, "");
                    MemberDTO groupMember = gitlabServiceClientOperator.queryGroupMember(devopsProjectDTO.getDevopsClusterEnvGroupId().intValue(), memberDTO.getId());
                    if (groupMember == null) {
                        gitlabServiceClientOperator.createGroupMember(devopsProjectDTO.getDevopsClusterEnvGroupId().intValue(), memberDTO);
                    }
                }
        );
    }

    private void createGroup(GitlabGroupPayload gitlabGroupPayload, final String suffix) {
        GroupDTO group = new GroupDTO();

        // name: orgName-projectName + suffix
        String name = GitOpsUtil.renderGroupName(gitlabGroupPayload.getOrganizationName(),
                gitlabGroupPayload.getProjectName(), suffix);
        // path: orgName-projectCode + suffix
        String path = GitOpsUtil.renderGroupPath(gitlabGroupPayload.getOrganizationCode(),
                gitlabGroupPayload.getProjectCode(), suffix);
        group.setName(name);
        group.setPath(path);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(gitlabGroupPayload.getUserId());
        if (userAttrDTO == null) {
            throw new CommonException("error.gitlab.user.sync.failed");
        }
        LOGGER.info("groupPath:{},adminId:{}", group.getPath(), GitUserNameUtil.getAdminId());
        GroupDTO groupDTO = gitlabServiceClientOperator.queryGroupByName(group.getPath(), TypeUtil.objToInteger(GitUserNameUtil.getAdminId()));
        if (groupDTO == null) {
            //admin创建组
            groupDTO = gitlabServiceClientOperator.createGroup(group, TypeUtil.objToInteger(GitUserNameUtil.getAdminId()));
        }
        LOGGER.info("groupDTO:{}", groupDTO);

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
        String name = GitOpsUtil.renderGroupName(gitlabGroupPayload.getOrganizationName(),
                gitlabGroupPayload.getProjectName(), suffix);
        // path: orgName-projectCode + suffix
        String path = GitOpsUtil.renderGroupPath(gitlabGroupPayload.getOrganizationCode(),
                gitlabGroupPayload.getProjectCode(), suffix);
        group.setName(name);
        group.setPath(path);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(gitlabGroupPayload.getUserId());
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(gitlabGroupPayload.getProjectId());

        Integer groupId = getCertainGroupIdBySuffix(suffix, devopsProjectDTO);

        if (groupId == null) {
            if (suffix.equals(CLUSTER_ENV_GROUP_SUFFIX)) {
                return;
            }
            throw new CommonException("error.group.id.get");
        }

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
                break;
            case ENV_GROUP_SUFFIX:
                devopsProjectDTO.setDevopsEnvGroupId(groupId);
                break;
            case CLUSTER_ENV_GROUP_SUFFIX:
                devopsProjectDTO.setDevopsClusterEnvGroupId(groupId);
                break;
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
