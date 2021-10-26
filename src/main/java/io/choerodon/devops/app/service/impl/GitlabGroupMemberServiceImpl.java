package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.GitlabGroupMemberVO;
import io.choerodon.devops.api.vo.kubernetes.MemberHelper;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.GitLabUserDTO;
import io.choerodon.devops.infra.dto.gitlab.GitlabProjectDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.UserProjectLabelVO;
import io.choerodon.devops.infra.enums.AccessLevel;
import io.choerodon.devops.infra.enums.EnvironmentType;
import io.choerodon.devops.infra.enums.GitlabGroupType;
import io.choerodon.devops.infra.enums.LabelType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
import io.choerodon.devops.infra.mapper.DevopsProjectMapper;
import io.choerodon.devops.infra.util.LogUtil;
import io.choerodon.devops.infra.util.TypeUtil;


/**
 * Created Zenger qs on 2018/3/28.
 */
@Service
public class GitlabGroupMemberServiceImpl implements GitlabGroupMemberService {
    private static final String ERROR_GITLAB_GROUP_ID_SELECT = "error.gitlab.groupId.select";
    private static final String PROJECT = "project";

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabGroupMemberServiceImpl.class);
    /**
     * devops项目类型
     */
    private static final String DEVOPS = "N_DEVOPS";

    @Autowired
    private DevopsProjectService devopsProjectService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private AppServiceService applicationService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper;
    @Autowired
    private DevopsEnvUserPermissionService devopsEnvUserPermissionService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsProjectMapper devopsProjectMapper;


    @Override
    public void createGitlabGroupMemberRole(List<GitlabGroupMemberVO> gitlabGroupMemberVOList, boolean isCreateUser) {
        List<GitlabGroupMemberVO> tempList = new ArrayList<>(gitlabGroupMemberVOList);
        tempList.forEach(t -> {
            if (t.getResourceType().equals(ResourceLevel.PROJECT.value())) {
                if (!baseServiceClientOperator.listProjectCategoryById(t.getResourceId()).contains(DEVOPS)) {
                    gitlabGroupMemberVOList.remove(t);
                }
            }
        });
        if (CollectionUtils.isEmpty(gitlabGroupMemberVOList)) {
            return;
        }
        gitlabGroupMemberVOList.stream()
                .filter(gitlabGroupMemberVO -> gitlabGroupMemberVO.getResourceType().equals(ResourceLevel.PROJECT.value()))
                .forEach(gitlabGroupMemberVO -> {
                    try {
                        List<String> userMemberRoleList = gitlabGroupMemberVO.getRoleLabels();
                        if (CollectionUtils.isEmpty(userMemberRoleList)) {
                            LOGGER.info("user member role is empty");
                            // 用户成员角色为空时，相当于删除该用户在此项目下的成员角色
                            deleteGitlabGroupMemberRole(gitlabGroupMemberVOList);
                            return;
                        }
                        MemberHelper memberHelper = getGitlabGroupMemberRole(userMemberRoleList);
                        operation(gitlabGroupMemberVO.getResourceId(),
                                gitlabGroupMemberVO.getResourceType(),
                                memberHelper,
                                gitlabGroupMemberVO.getUserId());
                    } catch (Exception e) {
                        if (e.getMessage().equals(ERROR_GITLAB_GROUP_ID_SELECT)) {
                            LOGGER.info(ERROR_GITLAB_GROUP_ID_SELECT);
                            return;
                        }
                        throw new CommonException(e);
                    }
                });
        // 根据标签如果是组织管理员需要添加组织下所有项目的的三个组的owner权限
        // 同步到gitlab
        gitlabGroupMemberVOList.stream()
                .filter(gitlabGroupMemberVO -> gitlabGroupMemberVO.getResourceType().equals(ResourceLevel.ORGANIZATION.value()))
                .forEach(member -> doCreateOrUpdateWithOrgLabel(member, isCreateUser));
    }

    /**
     * 处理用户的组织相关权限的创建或更新
     *
     * @param gitlabGroupMemberVO 用户信息
     */
    private void doCreateOrUpdateWithOrgLabel(GitlabGroupMemberVO gitlabGroupMemberVO, boolean isCreateUser) {
        List<String> roleLabels = gitlabGroupMemberVO.getRoleLabels();
        // 如果是组织管理员
        if (!CollectionUtils.isEmpty(roleLabels) && roleLabels.contains(LabelType.TENANT_ADMIN.getValue())) {
            List<ProjectDTO> projectDTOS = baseServiceClientOperator.listIamProjectByOrgId(gitlabGroupMemberVO.getResourceId());
            if (!CollectionUtils.isEmpty(projectDTOS)) {
                projectDTOS.forEach(projectDTO -> assignGitLabGroupMemberForOwner(projectDTO, gitlabGroupMemberVO.getUserId()));
            }
        } else {
            // 创建用户时不需要删除之前的权限
            // 此用户现在不是组织管理员，但是原本的此用户可能是组织管理员,需要将此组织下所有的项目的group的权限判断一遍
            if (!isCreateUser) {
                // 如果这个字段为null, 说明没传，不走这个逻辑，直接进去删除，
                // 如果传了（即使为空数组），可以方便的判断权限的变更，避免轮询组织下所有的项目来操作权限
                // 如果更新前用户的label不包含组织层admin的标签，就跳过之后删除权限的逻辑 (2020-11-19)
                if (gitlabGroupMemberVO.getPreviousRoleLabels() != null
                        && !gitlabGroupMemberVO.getPreviousRoleLabels().contains(LabelType.TENANT_ADMIN.getValue())) {
                    return;
                }
                deleteGitLabPermissionsForOrgAdmin(gitlabGroupMemberVO);
            }
        }
    }


    private void deleteGitLabPermissionsForOrgAdmin(GitlabGroupMemberVO gitlabGroupMemberVO) {
        List<ProjectDTO> projectDTOS = baseServiceClientOperator.listIamProjectByOrgId(gitlabGroupMemberVO.getResourceId());
        deleteGitLabPermissionsForOrgAdmin(projectDTOS, gitlabGroupMemberVO);
    }

    private void deleteGitLabPermissionsForOrgAdmin(List<ProjectDTO> projectDTOS, GitlabGroupMemberVO gitlabGroupMemberVO) {
        if (CollectionUtils.isEmpty(projectDTOS)) {
            return;
        }
        Set<Long> projectIds = projectDTOS.stream().map(ProjectDTO::getId).collect(Collectors.toSet());
        List<UserProjectLabelVO> labels = baseServiceClientOperator.listRoleLabelsForUserInTheProject(gitlabGroupMemberVO.getUserId(), projectIds);
        Map<Long, UserProjectLabelVO> projectLabels = labels.stream().collect(Collectors.toMap(UserProjectLabelVO::getProjectId, Function.identity()));

        // 遍历项目，处理权限
        projectDTOS.forEach(projectDTO -> {
            // 获取用户在此项目下的现在的角色标签, 可能为null
            UserProjectLabelVO projectLabel = projectLabels.get(projectDTO.getId());
            if (projectLabel != null
                    && projectLabel.getRoleLabels() != null
                    && projectLabel.getRoleLabels().contains(LabelType.GITLAB_PROJECT_OWNER.getValue())) {
                // 如果在项目层有gitlab owner这个标签就不删除权限，没有的话清除掉之前admin带来的group的owner权限
                return;
            }

            List<String> categoryList = baseServiceClientOperator.listProjectCategoryById(projectDTO.getId());
            if (CollectionUtils.isEmpty(categoryList) || !categoryList.contains(DEVOPS)) {
                return;
            }
            LOGGER.info("start to delete gitlab org owner for project with id {} for user with id {}", projectDTO.getId(), gitlabGroupMemberVO.getUserId());

            deleteAllPermissionInProjectOfUser(gitlabGroupMemberVO, projectDTO.getId(), true);

            // 如果不是所有者, 同步一次项目层权限,
            // 假如用户在gitlab的project是develop权限, 但是在group是owner, 删除group的owner后,
            // project中的develop权限也会被gitlab删除
            UserProjectLabelVO label = projectLabels.get(projectDTO.getId());
            MemberHelper memberHelper = getGitlabGroupMemberRole(label == null || CollectionUtils.isEmpty(label.getRoleLabels()) ? Collections.emptyList() : new ArrayList<>(label.getRoleLabels()));
            operation(projectDTO.getId(),
                    ResourceLevel.PROJECT.value(),
                    memberHelper,
                    gitlabGroupMemberVO.getUserId());
        });
    }


    @Override
    public void deleteGitlabGroupMemberRole(List<GitlabGroupMemberVO> gitlabGroupMemberVOList) {
        gitlabGroupMemberVOList.stream()
                .filter(gitlabGroupMemberVO -> gitlabGroupMemberVO.getResourceType().equals(PROJECT))
                .forEach(gitlabGroupMemberVO -> {
                    //删除用户的项目所有者权限，如果是组织root,则不删除该项目下gitlab相应的权限
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(gitlabGroupMemberVO.getResourceId());
                    if (!baseServiceClientOperator.isOrganzationRoot(gitlabGroupMemberVO.getUserId(), projectDTO.getOrganizationId())) {
                        deleteAllPermissionInProjectOfUser(gitlabGroupMemberVO, projectDTO.getId());
                    }
                });
        //组织root的标签，那么删除在组织下的root的权限
        // 列表中所有用户应该都是同一个组织的
        List<ProjectDTO> projectDTOS = null;
        for (GitlabGroupMemberVO gitlabGroupMemberVO : gitlabGroupMemberVOList) {
            // 项目层上面已经处理了,这里只处理组织层的
            if (gitlabGroupMemberVO.getResourceType().equals(ResourceLevel.ORGANIZATION.value())) {
                // 这里是避免多次查询
                if (projectDTOS == null) {
                    projectDTOS = baseServiceClientOperator.listIamProjectByOrgId(gitlabGroupMemberVO.getResourceId());
                }
                deleteGitLabPermissionsForOrgAdmin(projectDTOS, gitlabGroupMemberVO);
            }
        }
    }


    private void deleteAllPermissionInProjectOfUser(GitlabGroupMemberVO gitlabGroupMemberVO, Long projectId) {
        deleteAllPermissionInProjectOfUser(gitlabGroupMemberVO, projectId, false);
    }

    /**
     * 删除项目下的用户的权限
     *
     * @param gitlabGroupMemberVO 权限信息
     * @param projectId           项目id
     * @param skipNullProject     是否跳过未同步的项目(创建项目事务失败的项目)
     *                            一般是组织层的权限分配会需要跳过
     */
    private void deleteAllPermissionInProjectOfUser(GitlabGroupMemberVO gitlabGroupMemberVO, Long projectId, boolean skipNullProject) {
        List<String> categoryList = baseServiceClientOperator.listProjectCategoryById(projectId);
        if (CollectionUtils.isEmpty(categoryList) || !categoryList.contains(DEVOPS)) {
            return;
        }
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(gitlabGroupMemberVO.getUserId());
        userAttrService.checkUserSync(userAttrDTO, gitlabGroupMemberVO.getUserId());
        Integer gitlabUserId = TypeUtil.objToInteger(userAttrDTO.getGitlabUserId());
        GitLabUserDTO gitlabUserDTO = gitlabServiceClientOperator.queryUserById(
                TypeUtil.objToInteger(gitlabUserId));
        if (gitlabUserDTO == null) {
            LOGGER.error("error.gitlab.username.select");
            return;
        }
        DevopsProjectDTO devopsProjectDTO;
        MemberDTO memberDTO;
        if (skipNullProject) {
            // 组织层的用户的角色同步不应该因为项目事务失败导致用户权限同步失败
            devopsProjectDTO = devopsProjectService.queryWithoutCheck(projectId);
            // 这里判断appGroupId是因为脏数据可能为空的情况
            if (devopsProjectDTO == null || devopsProjectDTO.getDevopsAppGroupId() == null || devopsProjectDTO.getDevopsEnvGroupId() == null) {
                LOGGER.warn("Skip to sync permission to project with id {} due to null DevOps project. the permission info is {}", projectId, gitlabGroupMemberVO);
                return;
            }
        } else {
            devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);
        }

        // 删除应用服务对应gitlab的权限
        memberDTO = gitlabServiceClientOperator.queryGroupMember(
                TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()), gitlabUserId);
        if (memberDTO != null && memberDTO.getId() != null) {
            deleteGitlabRole(memberDTO, devopsProjectDTO, gitlabUserId, GitlabGroupType.APP);
        }

        // 删除gitops对应gitlab的权限
        memberDTO = gitlabServiceClientOperator.queryGroupMember(
                TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId()), gitlabUserId);
        if (memberDTO != null && memberDTO.getId() != null) {
            deleteGitlabRole(memberDTO, devopsProjectDTO, gitlabUserId, GitlabGroupType.ENV_GITOPS);
        }
        deleteAboutApplicationService(gitlabGroupMemberVO.getResourceId(), userAttrDTO.getGitlabUserId().intValue(), userAttrDTO.getIamUserId());
        deleteAboutEnvironment(gitlabGroupMemberVO.getResourceId(), userAttrDTO.getGitlabUserId().intValue(), userAttrDTO.getIamUserId());

        // 删除集群环境对应gitlab的权限
        if (devopsProjectDTO.getDevopsClusterEnvGroupId() != null) {
            memberDTO = gitlabServiceClientOperator.queryGroupMember(
                    TypeUtil.objToInteger(devopsProjectDTO.getDevopsClusterEnvGroupId()), gitlabUserId);
            if (memberDTO != null && memberDTO.getId() != null) {
                deleteGitlabRole(memberDTO, devopsProjectDTO, gitlabUserId, GitlabGroupType.CLUSTER_GITOPS);
            }
            deleteAboutCluster(gitlabGroupMemberVO.getResourceId(), userAttrDTO.getGitlabUserId().intValue(), userAttrDTO.getIamUserId());
        }
    }

    /**
     * 删除角色时处理应用相关的操作
     *
     * @param projectId    项目id
     * @param gitlabUserId gitlab 用户id
     * @param userId       用户id
     */
    private void deleteAboutApplicationService(Long projectId, Integer gitlabUserId, Long userId) {
        List<AppServiceDTO> applicationDTOS = applicationService.baseListByProjectId(projectId)
                .stream()
                .filter(app -> app.getGitlabProjectId() != null)
                .collect(Collectors.toList());

        // 删除角色时同时清除应用的gitlab的权限和在devops的应用-用户权限分配记录
        applicationDTOS.forEach(app -> {
            MemberDTO projectMember = gitlabServiceClientOperator.getProjectMember(app.getGitlabProjectId(), gitlabUserId);
            // 只删除gitlab.owner角色
            if (projectMember != null
                    && projectMember.getId() != null
                    && AccessLevel.OWNER.value.equals(projectMember.getAccessLevel())) {
                gitlabServiceClientOperator.deleteProjectMember(app.getGitlabProjectId(), gitlabUserId);
            }
        });
    }

    /**
     * 删除角色时处理环境相关的操作
     *
     * @param projectId    项目id
     * @param gitlabUserId gitlab 用户id
     * @param userId       用户id
     */
    private void deleteAboutEnvironment(Long projectId, Integer gitlabUserId, Long userId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO();
        devopsEnvironmentDTO.setProjectId(projectId);
        devopsEnvironmentDTO.setType(EnvironmentType.USER.getValue());
        devopsEnvironmentMapper.select(devopsEnvironmentDTO)
                .stream()
                .peek(env -> devopsEnvUserPermissionService.baseDelete(env.getId(), userId))
                .filter(env -> !env.getSynchro().equals(Boolean.FALSE))
                .forEach(env -> {
                    MemberDTO projectMember = gitlabServiceClientOperator.getProjectMember(env.getGitlabEnvProjectId().intValue(), gitlabUserId);
                    if (projectMember != null && projectMember.getId() != null) {
                        gitlabServiceClientOperator.deleteProjectMember(env.getGitlabEnvProjectId().intValue(), gitlabUserId);
                    }
                });
    }

    /**
     * 删除角色时处理集群相关的操作
     *
     * @param projectId    项目id
     * @param gitlabUserId gitlab 用户id
     * @param userId       用户id
     */
    private void deleteAboutCluster(Long projectId, Integer gitlabUserId, Long userId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO();
        devopsEnvironmentDTO.setProjectId(projectId);
        devopsEnvironmentDTO.setType(EnvironmentType.SYSTEM.getValue());
        devopsEnvironmentMapper.select(devopsEnvironmentDTO)
                .stream()
                .filter(env -> !env.getSynchro().equals(Boolean.FALSE))
                .forEach(env -> {
                    MemberDTO projectMember = gitlabServiceClientOperator.getProjectMember(env.getGitlabEnvProjectId().intValue(), gitlabUserId);
                    if (projectMember != null && projectMember.getId() != null) {
                        gitlabServiceClientOperator.deleteProjectMember(env.getGitlabEnvProjectId().intValue(), gitlabUserId);
                    }
                });
    }

    @Override
    public void checkEnvProject(DevopsEnvironmentDTO devopsEnvironmentDTO, UserAttrDTO userAttrDTO) {
        DevopsProjectDTO devopsProjectDTO = devopsProjectService
                .baseQueryByProjectId(devopsEnvironmentDTO.getProjectId());
        if (devopsEnvironmentDTO.getGitlabEnvProjectId() == null) {
            throw new CommonException("error.env.project.not.exist");
        }
        // 跳过Root用户
        if (Boolean.TRUE.equals(userAttrDTO.getGitlabAdmin())) {
            return;
        }

        MemberDTO memberDTO = gitlabServiceClientOperator
                .queryGroupMember(TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId()),
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));

        if (memberDTO != null && memberDTO.getAccessLevel().equals(AccessLevel.OWNER.toValue())) {
            return;
        }

        MemberDTO newGroupMemberDTO = gitlabServiceClientOperator.getProjectMember(
                TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        if (newGroupMemberDTO == null || !(newGroupMemberDTO.getAccessLevel().equals(AccessLevel.MASTER.toValue()))) {
            throw new CommonException("error.user.not.env.pro.owner");
        }
    }

    @Override
    public MemberDTO queryByUserId(Integer groupId, Integer userId) {
        return gitlabServiceClientOperator.queryGroupMember(groupId, userId);
    }

    @Override
    public void delete(Integer groupId, Integer userId) {
        gitlabServiceClientOperator.deleteGroupMember(groupId, userId);
    }

    @Override
    public int create(Integer groupId, MemberDTO memberDTO) {
        return gitlabServiceClientOperator.createGroupMember(groupId, memberDTO);
    }

    @Override
    public void update(Integer groupId, MemberDTO memberDTO) {
        gitlabServiceClientOperator.updateGroupMember(groupId, memberDTO);
    }

    /**
     * get AccessLevel
     *
     * @param userMemberRoleList userMemberRoleList
     */
    private MemberHelper getGitlabGroupMemberRole(List<String> userMemberRoleList) {
        MemberHelper memberHelper = new MemberHelper();
        // 目前只支持 Owner 和 Developer
        userMemberRoleList.forEach(level -> AccessLevel.forString(level.toUpperCase(), memberHelper));
        return memberHelper;
    }

    /**
     * The user action
     *
     * @param resourceId   资源Id
     * @param resourceType 资源type
     * @param memberHelper memberHelper
     * @param userId       userId
     */
    private void operation(Long resourceId, String resourceType, MemberHelper memberHelper, Long userId) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(userId);
        if (userAttrDTO == null) {
            throw new CommonException("The user you want to assign a role to is not created successfully!");
        }
        Integer gitlabUserId = TypeUtil.objToInteger(userAttrDTO.getGitlabUserId());
        DevopsProjectDTO devopsProjectDTO;
        MemberDTO memberDTO;
        Integer[] roles = {
                memberHelper.getProjectDevelopAccessLevel().toValue(),
                memberHelper.getProjectOwnerAccessLevel().toValue()};
        AccessLevel accessLevel = AccessLevel.forValue(Collections.max(Arrays.asList(roles)));
        IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(userAttrDTO.getIamUserId());
        if (Objects.isNull(iamUserDTO)) {
            LogUtil.loggerInfoObjectNullWithId("user", userAttrDTO.getIamUserId(), LOGGER);
            return;
        }
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(resourceId);
        // 如果当前iam用户只有项目成员的权限,并且他不是这个组织的组织管理员
        if (AccessLevel.DEVELOPER.equals(accessLevel)
                && !baseServiceClientOperator.isOrganzationRoot(iamUserDTO.getId(), projectDTO.getOrganizationId())) {
            LOGGER.debug("Access level is develop for user with id {} in project {}", userId, resourceId);
            // 查看是不是由项目所有者改为项目成员
            devopsProjectDTO = devopsProjectService.baseQueryByProjectId(resourceId);

            memberDTO = gitlabServiceClientOperator.queryGroupMember(
                    TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()),
                    (TypeUtil.objToInteger(userAttrDTO.getGitlabUserId())));
            if (memberDTO != null && AccessLevel.OWNER.toValue().equals(memberDTO.getAccessLevel())) {
                deleteGitlabRole(memberDTO, devopsProjectDTO, gitlabUserId, GitlabGroupType.APP);
            }

            memberDTO = gitlabServiceClientOperator.queryGroupMember(
                    TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId()),
                    (TypeUtil.objToInteger(userAttrDTO.getGitlabUserId())));
            if (memberDTO != null && AccessLevel.OWNER.toValue().equals(memberDTO.getAccessLevel())) {
                deleteGitlabRole(memberDTO, devopsProjectDTO, gitlabUserId, GitlabGroupType.ENV_GITOPS);
            }

            if (devopsProjectDTO.getDevopsClusterEnvGroupId() != null) {
                memberDTO = gitlabServiceClientOperator.queryGroupMember(
                        TypeUtil.objToInteger(devopsProjectDTO.getDevopsClusterEnvGroupId()),
                        (TypeUtil.objToInteger(userAttrDTO.getGitlabUserId())));
                if (memberDTO != null && AccessLevel.OWNER.toValue().equals(memberDTO.getAccessLevel())) {
                    deleteGitlabRole(memberDTO, devopsProjectDTO, gitlabUserId, GitlabGroupType.CLUSTER_GITOPS);
                }
            }
            // 为当前项目下所有跳过权限检查的环境库加上gitlab用户权限
            addRoleForSkipPermissionEnvironment(resourceId, gitlabUserId);
        } else if (AccessLevel.OWNER.equals(accessLevel)) {
            LOGGER.debug("Access level is owner for user with id {} in project {}", userId, resourceId);
            // 删除用户时同时清除gitlab的权限
            List<Integer> gitlabProjectIds = applicationService
                    .baseListByProjectId(resourceId).stream().filter(e -> e.getGitlabProjectId() != null)
                    .map(AppServiceDTO::getGitlabProjectId).map(TypeUtil::objToInteger)
                    .collect(Collectors.toList());
            gitlabProjectIds.forEach(e -> {
                MemberDTO projectMember = gitlabServiceClientOperator.getProjectMember(e, gitlabUserId);
                if (projectMember != null && projectMember.getId() != null) {
                    gitlabServiceClientOperator.deleteProjectMember(e, gitlabUserId);
                }
            });

            devopsProjectDTO = devopsProjectService.baseQueryByProjectId(resourceId);

            // 用户此时为项目所有者角色，删除应用服务权限关联表和环境权限关联表中的数据
            deletePermissionUserRelation(devopsProjectDTO.getIamProjectId(), userId);

            // 给gitlab应用组分配owner角色
            memberDTO = gitlabServiceClientOperator.queryGroupMember(
                    TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()),
                    (TypeUtil.objToInteger(userAttrDTO.getGitlabUserId())));
            addOrUpdateGitlabRole(accessLevel, memberDTO,
                    TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()), userAttrDTO);

            //给gitlab环境组分配owner角色
            memberDTO = gitlabServiceClientOperator.queryGroupMember(
                    TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId()),
                    (TypeUtil.objToInteger(userAttrDTO.getGitlabUserId())));
            addOrUpdateGitlabRole(accessLevel, memberDTO,
                    TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId()), userAttrDTO);

            //给gitlab集群组分配owner角色
            if (devopsProjectDTO.getDevopsClusterEnvGroupId() != null) {
                memberDTO = gitlabServiceClientOperator.queryGroupMember(
                        TypeUtil.objToInteger(devopsProjectDTO.getDevopsClusterEnvGroupId()),
                        (TypeUtil.objToInteger(userAttrDTO.getGitlabUserId())));
                addOrUpdateGitlabRole(accessLevel, memberDTO,
                        TypeUtil.objToInteger(devopsProjectDTO.getDevopsClusterEnvGroupId()), userAttrDTO);
            }

        }
        LOGGER.debug("Finish member role operation for user with id {} and access level {}", userId, accessLevel);
    }

    private void deletePermissionUserRelation(Long projectId, Long userId) {
        // 查出项目下不跳过权限的所有环境
        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO();
        devopsEnvironmentDTO.setProjectId(projectId);
        devopsEnvironmentDTO.setSkipCheckPermission(false);
        devopsEnvironmentDTO.setType(EnvironmentType.USER.getValue());
        List<Long> envIdsWithNoSkipCheck = devopsEnvironmentMapper.select(devopsEnvironmentDTO)
                .stream()
                .map(DevopsEnvironmentDTO::getId)
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(envIdsWithNoSkipCheck)) {
            devopsEnvUserPermissionService.batchDelete(envIdsWithNoSkipCheck, userId);
        }
    }


    /**
     * add member roles in gitlab projects for developer and environment gitlab projects that skip permission check.
     *
     * @param projectId    member role project id
     * @param gitlabUserId user's gitlab id
     */
    private void addRoleForSkipPermissionEnvironment(Long projectId, Integer gitlabUserId) {
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);
        Integer groupId = TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId());
        // 拒绝组里的AccessRequest
        gitlabServiceClientOperator.denyAccessRequest(groupId, gitlabUserId);

        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO();
        devopsEnvironmentDTO.setProjectId(projectId);
        devopsEnvironmentDTO.setSkipCheckPermission(Boolean.TRUE);
        devopsEnvironmentDTO.setType(EnvironmentType.USER.getValue());
        devopsEnvironmentMapper.select(devopsEnvironmentDTO)
                .stream()
                .filter(app -> app.getGitlabEnvProjectId() != null)
                .map(DevopsEnvironmentDTO::getGitlabEnvProjectId)
                .forEach(gitlabProjectId -> {
                    GitlabProjectDTO gitlabProjectDO = null;
                    try {
                        gitlabProjectDO = gitlabServiceClientOperator.queryProjectById(gitlabProjectId.intValue());
                    } catch (CommonException exception) {
                        LOGGER.info("project not found");
                    }

                    if (gitlabProjectDO != null && gitlabProjectDO.getId() != null) {
                        // 删除组和用户之间的关系，如果存在
                        MemberDTO memberDTO = queryByUserId(groupId, gitlabUserId);
                        if (memberDTO != null) {
                            delete(groupId, gitlabUserId);
                        }
                        // 当项目不存在用户权限纪录时(防止失败重试时报成员已存在异常)，添加gitlab用户权限
                        MemberDTO gitlabMemberDTO = gitlabServiceClientOperator.getProjectMember(gitlabProjectId.intValue(), gitlabUserId);
                        if (gitlabMemberDTO == null || gitlabMemberDTO.getId() == null) {
                            gitlabServiceClientOperator.createProjectMember(gitlabProjectId.intValue(), new MemberDTO(gitlabUserId, AccessLevel.MASTER.value, ""));
                        }
                    }
                });
    }


    private void addOrUpdateGitlabRole(AccessLevel level, MemberDTO memberDTO, Integer groupId,
                                       UserAttrDTO userAttrDTO) {
        // 增删改用户
        switch (level) {
            case NONE:
                if (memberDTO != null) {
                    gitlabServiceClientOperator
                            .deleteGroupMember(groupId, (TypeUtil.objToInteger(userAttrDTO.getGitlabUserId())));
                }
                break;
            case DEVELOPER:
            case MASTER:
            case OWNER:
                MemberDTO requestMember = new MemberDTO((TypeUtil.objToInteger(userAttrDTO.getGitlabUserId())), level.toValue(), "");
                if (memberDTO == null) {
                    gitlabServiceClientOperator.createGroupMember(groupId, requestMember);
                } else {
                    if (!Objects.equals(requestMember.getAccessLevel(), memberDTO.getAccessLevel())) {
                        gitlabServiceClientOperator.updateGroupMember(groupId, requestMember);
                    }
                }
                break;
            default:
                LOGGER.error("error.gitlab.member.level");
                break;
        }
    }

    private void deleteGitlabRole(MemberDTO memberDTO, DevopsProjectDTO devopsProjectDTO,
                                  Integer userId, GitlabGroupType gitlabGroupType) {
        if (memberDTO != null) {
            Integer gitlabGroupId;
            switch (gitlabGroupType) {
                case APP:
                    gitlabGroupId = TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId());
                    break;
                case ENV_GITOPS:
                    gitlabGroupId = TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId());
                    break;
                case CLUSTER_GITOPS:
                    gitlabGroupId = TypeUtil.objToInteger(devopsProjectDTO.getDevopsClusterEnvGroupId());
                    break;
                default:
                    throw new CommonException("error.gitlab.group.type");
            }
            gitlabServiceClientOperator.deleteGroupMember(gitlabGroupId, userId);
        }
    }


    public void assignGitLabGroupOwner(Long groupId, MemberDTO groupMemberDTO, MemberDTO memberDTO) {
        if (Objects.isNull(groupMemberDTO)) {
            gitlabServiceClientOperator.createGroupMember(TypeUtil.objToInteger(groupId), memberDTO);
        } else {
            if (!AccessLevel.OWNER.toValue().equals(groupMemberDTO.getAccessLevel())) {
                gitlabServiceClientOperator.updateGroupMember(TypeUtil.objToInteger(groupId), memberDTO);
            }
        }
    }

    public void assignGitLabGroupMemberForOwner(ProjectDTO projectDTO, Long userId) {
        List<String> categoryList = baseServiceClientOperator.listProjectCategoryById(projectDTO.getId());
        if (CollectionUtils.isEmpty(categoryList) || !categoryList.contains(DEVOPS)) {
            return;
        }
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(userId);
        DevopsProjectDTO search = new DevopsProjectDTO();
        search.setIamProjectId(projectDTO.getId());
        DevopsProjectDTO devopsProjectDTO = devopsProjectMapper.selectOne(search);
        if (!Objects.isNull(devopsProjectDTO) && !Objects.isNull(userAttrDTO)) {
            MemberDTO memberDTO = new MemberDTO((TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()))
                    , AccessLevel.OWNER.toValue(), "");
            //分配gitlab应用服务owner  注意跳过没有的项目
            if (!Objects.isNull(devopsProjectDTO.getDevopsAppGroupId())) {
                MemberDTO appMemberDTO = gitlabServiceClientOperator.queryGroupMember(
                        TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
                assignGitLabGroupOwner(devopsProjectDTO.getDevopsAppGroupId(), appMemberDTO, memberDTO);
            }
            if (!Objects.isNull(devopsProjectDTO.getDevopsEnvGroupId())) {
                //添加环境的owner权限
                MemberDTO envMemberDTO1 = gitlabServiceClientOperator.queryGroupMember(TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId()), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
                assignGitLabGroupOwner(devopsProjectDTO.getDevopsEnvGroupId(), envMemberDTO1, memberDTO);
            }

            //添加集群配置库的owner,一些旧项目的集群group是采用懒加载的方式创建的,组可能为null
            if (!Objects.isNull(devopsProjectDTO.getDevopsClusterEnvGroupId())) {
                MemberDTO clusterMemberDTO1 = gitlabServiceClientOperator.queryGroupMember(TypeUtil.objToInteger(devopsProjectDTO.getDevopsClusterEnvGroupId()), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
                assignGitLabGroupOwner(devopsProjectDTO.getDevopsClusterEnvGroupId(), clusterMemberDTO1, memberDTO);
            }
        } else {
            LOGGER.warn("assignGitLabGroupMemberForOwner skip due to empty project with id {}", projectDTO.getId());
        }
    }
}
