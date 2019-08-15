package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.GitlabGroupMemberVO;
import io.choerodon.devops.api.vo.kubernetes.MemberHelper;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.GitLabUserDTO;
import io.choerodon.devops.infra.dto.gitlab.GitlabProjectDTO;
import io.choerodon.devops.infra.dto.gitlab.GroupDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.enums.AccessLevel;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
import io.choerodon.devops.infra.util.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Created Zenger qs on 2018/3/28.
 */
@Service
public class GitlabGroupMemberServiceImpl implements GitlabGroupMemberService {
    public static final String ERROR_GITLAB_GROUP_ID_SELECT = "error.gitlab.groupId.select";
    private static final String PROJECT = "project";
    private static final String TEMPLATE = "template";
    private static final String SITE = "site";

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabGroupMemberServiceImpl.class);

    @Autowired
    private DevopsProjectService devopsProjectService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private IamService iamService;
    @Autowired
    private AppServiceService applicationService;
    @Autowired
    private AppServiceUserPermissionService appServiceUserPermissionService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper;
    @Autowired
    private DevopsEnvUserPermissionService devopsEnvUserPermissionService;


    @Override
    public void createGitlabGroupMemberRole(List<GitlabGroupMemberVO> gitlabGroupMemberVOList) {
        gitlabGroupMemberVOList.stream()
                .filter(gitlabGroupMemberVO -> gitlabGroupMemberVO.getResourceType().equals(PROJECT))
                .forEach(gitlabGroupMemberVO -> {
                    try {
                        List<String> userMemberRoleList = gitlabGroupMemberVO.getRoleLabels();
                        if (userMemberRoleList == null) {
                            userMemberRoleList = new ArrayList<>();
                            LOGGER.info("user member role is empty");
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
    }

    @Override
    public void deleteGitlabGroupMemberRole(List<GitlabGroupMemberVO> gitlabGroupMemberVOList) {
        gitlabGroupMemberVOList.stream()
                .filter(gitlabGroupMemberVO -> gitlabGroupMemberVO.getResourceType().equals(PROJECT))
                .forEach(gitlabGroupMemberVO -> {
                    UserAttrDTO userAttrDTO = userAttrService.baseQueryById(gitlabGroupMemberVO.getUserId());
                    Integer gitlabUserId = TypeUtil.objToInteger(userAttrDTO.getGitlabUserId());
                    GitLabUserDTO gitlabUserDTO = gitlabServiceClientOperator.queryUserById(
                            TypeUtil.objToInteger(gitlabUserId));
                    if (gitlabUserDTO == null) {
                        LOGGER.error("error.gitlab.username.select");
                        return;
                    }
                    DevopsProjectDTO devopsProjectDTO;
                    MemberDTO memberDTO;
                        devopsProjectDTO = devopsProjectService.baseQueryByProjectId(gitlabGroupMemberVO.getResourceId());
                        memberDTO = gitlabServiceClientOperator.queryGroupMember(
                                TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()), gitlabUserId);
                        if (memberDTO != null && memberDTO.getUserId() != null) {
                            deleteGitlabRole(memberDTO, devopsProjectDTO, gitlabUserId, false);
                        }
                        memberDTO = gitlabServiceClientOperator.queryGroupMember(
                                TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId()), gitlabUserId);
                        if (memberDTO != null && memberDTO.getUserId() != null) {
                            deleteGitlabRole(memberDTO, devopsProjectDTO, gitlabUserId, true);
                        }

                        deleteAboutApplicationService(gitlabGroupMemberVO.getResourceId(), userAttrDTO.getGitlabUserId().intValue(), userAttrDTO.getIamUserId());
                        deleteAboutEnvironment(gitlabGroupMemberVO.getResourceId(), userAttrDTO.getGitlabUserId().intValue(), userAttrDTO.getIamUserId());
                });
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
            if (projectMember != null && projectMember.getUserId() != null) {
                gitlabServiceClientOperator.deleteProjectMember(app.getGitlabProjectId(), gitlabUserId);
            }
        });

        appServiceUserPermissionService.baseDeleteByUserIdAndAppIds(
                applicationDTOS.stream().map(AppServiceDTO::getId).collect(Collectors.toList()),
                userId);
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
        devopsEnvironmentMapper.select(devopsEnvironmentDTO)
                .stream()
                .peek(env -> devopsEnvUserPermissionService.baseDelete(env.getId(), userId))
                .filter(env -> env.getSynchro() != Boolean.FALSE)
                .forEach(env -> {
                    MemberDTO projectMember = gitlabServiceClientOperator.getProjectMember(env.getGitlabEnvProjectId().intValue(), gitlabUserId);
                    if (projectMember != null && projectMember.getUserId() != null) {
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
        MemberDTO memberDTO = gitlabServiceClientOperator
                .queryGroupMember(TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId()),
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        if (memberDTO != null && memberDTO.getAccessLevel().equals(AccessLevel.OWNER.toValue())) {
            return;
        }
        MemberDTO newGroupMemberDTO = gitlabServiceClientOperator.getProjectMember(
                TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        if (newGroupMemberDTO == null || (newGroupMemberDTO.getAccessLevel().equals(AccessLevel.MASTER.toValue()))) {
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
        List<Integer> accessLevelList = new ArrayList<>();
        accessLevelList.add(0);
        userMemberRoleList.forEach(level -> {
            AccessLevel levels = AccessLevel.forString(level.toUpperCase(), memberHelper);
            switch (levels) {
                case OWNER:
                    accessLevelList.add(levels.toValue());
                    break;
                case MASTER:
                    accessLevelList.add(levels.toValue());
                    break;
                case DEVELOPER:
                    accessLevelList.add(levels.toValue());
                    break;
                default:
                    break;
            }
        });
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
                memberHelper.getProjectOwnerAccessLevel().toValue(),
                memberHelper.getOrganizationAccessLevel().toValue()};
        AccessLevel accessLevel = AccessLevel.forValue(Collections.max(Arrays.asList(roles)));
        // 如果当前iam用户只有项目成员的权限
        if (AccessLevel.DEVELOPER.equals(accessLevel)) {
            // 查看是不是由项目所有者改为项目成员
            devopsProjectDTO = devopsProjectService.baseQueryByProjectId(resourceId);
            memberDTO = gitlabServiceClientOperator.queryGroupMember(
                    TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()),
                    (TypeUtil.objToInteger(userAttrDTO.getGitlabUserId())));
            if (memberDTO != null && AccessLevel.OWNER.toValue().equals(memberDTO.getAccessLevel())) {
                deleteGitlabRole(memberDTO, devopsProjectDTO, gitlabUserId, false);
            }
            memberDTO = gitlabServiceClientOperator.queryGroupMember(
                    TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId()),
                    (TypeUtil.objToInteger(userAttrDTO.getGitlabUserId())));
            if (memberDTO != null && AccessLevel.OWNER.toValue().equals(memberDTO.getAccessLevel())) {
                deleteGitlabRole(memberDTO, devopsProjectDTO, gitlabUserId, true);
            }
            // 为当前项目下所有跳过权限检查的应用加上gitlab用户权限
            addRoleForSkipPermissionAppService(resourceId, gitlabUserId);
            // 为当前项目下所有跳过权限检查的环境库加上gitlab用户权限
            addRoleForSkipPermissionEnvironment(resourceId, gitlabUserId);
        } else if (AccessLevel.OWNER.equals(accessLevel)) {
                try {
                    // 删除用户时同时清除gitlab的权限
                    List<Integer> gitlabProjectIds = applicationService
                            .baseListByProjectId(resourceId).stream().filter(e -> e.getGitlabProjectId() != null)
                            .map(AppServiceDTO::getGitlabProjectId).map(TypeUtil::objToInteger)
                            .collect(Collectors.toList());
                    gitlabProjectIds.forEach(e -> {
                        MemberDTO projectMember = gitlabServiceClientOperator.getProjectMember(e, gitlabUserId);
                        if (projectMember != null && projectMember.getUserId() != null) {
                            gitlabServiceClientOperator.deleteProjectMember(e, gitlabUserId);
                        }
                    });
                    // 给gitlab应用组分配owner角色
                    devopsProjectDTO = devopsProjectService.baseQueryByProjectId(resourceId);
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

                } catch (Exception e) {
                    LOGGER.info(ERROR_GITLAB_GROUP_ID_SELECT);
                }
        }
    }

    /**
     * add member roles in gitlab projects for developer and application services that skip permission check.
     *
     * @param projectId    member role project id
     * @param gitlabUserId user's gitlab id
     */
    private void addRoleForSkipPermissionAppService(Long projectId, Integer gitlabUserId) {
        applicationService.baseListByProjectIdAndSkipCheck(projectId)
                .stream()
                .filter(app -> app.getGitlabProjectId() != null)
                .map(AppServiceDTO::getGitlabProjectId)
                .forEach(gitlabProjectId -> {
                    DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);
                    Integer groupId = devopsProjectDTO.getDevopsAppGroupId().intValue();
                    GitlabProjectDTO gitlabProjectDO = null;
                    try {
                        gitlabProjectDO = gitlabServiceClientOperator.queryProjectById(gitlabProjectId);
                    } catch (CommonException exception) {
                        LOGGER.info("project not found");
                    }

                    if (gitlabProjectDO != null && gitlabProjectDO.getId() != null) {
                        // 删除组和用户之间的关系，如果存在
                        MemberDTO memberDTO = queryByUserId(groupId, gitlabUserId);
                        if (memberDTO != null) {
                            delete(groupId, TypeUtil.objToInteger(gitlabUserId));
                        }
                        // 当项目不存在用户权限纪录时(防止失败重试时报成员已存在异常)，添加gitlab用户权限
                        MemberDTO gitlabMemberDTO = gitlabServiceClientOperator.getProjectMember(gitlabProjectId, gitlabUserId);
                        if (gitlabMemberDTO == null || gitlabMemberDTO.getUserId() == null) {
                            gitlabServiceClientOperator.createProjectMember(gitlabProjectId, new MemberDTO(gitlabUserId, 30, ""));
                        }
                    }
                });
    }


    /**
     * add member roles in gitlab projects for developer and environment gitlab projects that skip permission check.
     *
     * @param projectId    member role project id
     * @param gitlabUserId user's gitlab id
     */
    private void addRoleForSkipPermissionEnvironment(Long projectId, Integer gitlabUserId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO();
        devopsEnvironmentDTO.setProjectId(projectId);
        devopsEnvironmentDTO.setSkipCheckPermission(Boolean.TRUE);
        devopsEnvironmentMapper.select(devopsEnvironmentDTO)
                .stream()
                .filter(app -> app.getGitlabEnvProjectId() != null)
                .map(DevopsEnvironmentDTO::getGitlabEnvProjectId)
                .forEach(gitlabProjectId -> {
                    DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);
                    Integer groupId = devopsProjectDTO.getDevopsAppGroupId().intValue();
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
                        if (gitlabMemberDTO == null || gitlabMemberDTO.getUserId() == null) {
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
                MemberDTO requestMember = new MemberDTO();
                requestMember.setUserId((TypeUtil.objToInteger(userAttrDTO.getGitlabUserId())));
                requestMember.setAccessLevel(level.toValue());
                requestMember.setExpiresAt("");
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
                                  Integer userId, Boolean isEnvDelete) {
        if (memberDTO != null) {
            gitlabServiceClientOperator.deleteGroupMember(
                    isEnvDelete ? TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId())
                            : TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()), userId);
        }
    }
}
