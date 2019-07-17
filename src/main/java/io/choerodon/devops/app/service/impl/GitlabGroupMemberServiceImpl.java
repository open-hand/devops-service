package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.GitlabGroupMemberDTO;
import io.choerodon.devops.app.service.GitlabGroupMemberService;
import io.choerodon.devops.domain.application.repository.DevopsProjectRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.domain.application.valueobject.MemberHelper;
import io.choerodon.devops.domain.application.valueobject.OrganizationVO;
import io.choerodon.devops.infra.dataobject.gitlab.GitlabProjectDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.enums.AccessLevel;
import io.choerodon.devops.infra.feign.GitlabServiceClient;
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
    private DevopsProjectRepository devopsProjectRepository;
    @Autowired
    private GitlabUserRepository gitlabUserRepository;
    @Autowired
    private GitlabGroupMemberRepository gitlabGroupMemberRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private GitlabProjectRepository gitlabProjectRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private AppUserPermissionRepository appUserPermissionRepository;
    @Autowired
    private GitlabServiceClient gitlabServiceClient;


    @Override
    public void createGitlabGroupMemberRole(List<GitlabGroupMemberDTO> gitlabGroupMemberDTOList) {
        gitlabGroupMemberDTOList.stream()
                .filter(gitlabGroupMemberDTO -> !gitlabGroupMemberDTO.getResourceType().equals(SITE))
                .forEach(gitlabGroupMemberDTO -> {
                    try {
                        List<String> userMemberRoleList = gitlabGroupMemberDTO.getRoleLabels();
                        if (userMemberRoleList == null) {
                            userMemberRoleList = new ArrayList<>();
                            LOGGER.info("user member role is empty");
                        }
                        MemberHelper memberHelper = getGitlabGroupMemberRole(userMemberRoleList);
                        operation(gitlabGroupMemberDTO.getResourceId(),
                                gitlabGroupMemberDTO.getResourceType(),
                                memberHelper,
                                gitlabGroupMemberDTO.getUserId());
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
    public void deleteGitlabGroupMemberRole(List<GitlabGroupMemberDTO> gitlabGroupMemberDTOList) {
        gitlabGroupMemberDTOList.stream()
                .filter(gitlabGroupMemberDTO -> !gitlabGroupMemberDTO.getResourceType().equals(SITE))
                .forEach(gitlabGroupMemberDTO -> {
                    UserAttrE userAttrE = userAttrRepository.baseQueryById(gitlabGroupMemberDTO.getUserId());
                    Integer gitlabUserId = TypeUtil.objToInteger(userAttrE.getGitlabUserId());
                    GitlabUserE gitlabUserE = gitlabUserRepository.getGitlabUserByUserId(
                            TypeUtil.objToInteger(gitlabUserId));
                    if (gitlabUserE == null) {
                        LOGGER.error("error.gitlab.username.select");
                        return;
                    }
                    DevopsProjectVO devopsProjectE;
                    GitlabMemberE gitlabMemberE;
                    if (PROJECT.equals(gitlabGroupMemberDTO.getResourceType())) {
                        devopsProjectE = devopsProjectRepository.baseQueryByProjectId(gitlabGroupMemberDTO.getResourceId());
                        gitlabMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                                TypeUtil.objToInteger(devopsProjectE.getDevopsAppGroupId()), gitlabUserId);
                        if (gitlabMemberE != null && gitlabMemberE.getId() != null) {
                            deleteGilabRole(gitlabMemberE, devopsProjectE, gitlabUserId, false);
                        }
                        gitlabMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                                TypeUtil.objToInteger(devopsProjectE.getDevopsEnvGroupId()), gitlabUserId);
                        if (gitlabMemberE != null && gitlabMemberE.getId() != null) {
                            deleteGilabRole(gitlabMemberE, devopsProjectE, gitlabUserId, true);
                        }
                        // 删除用户时同时清除gitlab的权限
                        List<Integer> gitlabProjectIds = applicationRepository
                                .listByProjectId(gitlabGroupMemberDTO.getResourceId()).stream()
                                .filter(e -> e.getGitlabProjectE() != null)
                                .map(e -> e.getGitlabProjectE().getId()).map(TypeUtil::objToInteger)
                                .collect(Collectors.toList());
                        // gitlab

                        gitlabProjectIds.forEach(e -> {
                            GitlabMemberE memberE = gitlabProjectRepository.getProjectMember(e, gitlabUserId);
                            if (memberE != null && memberE.getId() != null) {
                                gitlabRepository.removeMemberFromProject(e, gitlabUserId);
                            }
                        });
                        // devops
                        appUserPermissionRepository.baseDeleteByUserIdAndAppIds(
                                applicationRepository.listByProjectId(gitlabGroupMemberDTO.getResourceId()).stream()
                                        .filter(applicationE -> applicationE.getGitlabProjectE() != null)
                                        .map(ApplicationE::getId).collect(Collectors.toList()),
                                userAttrE.getIamUserId());
                    } else {
                        OrganizationVO organization =
                                iamRepository.queryOrganizationById(gitlabGroupMemberDTO.getResourceId());
                        devopsProjectE = gitlabRepository.queryGroupByName(
                                organization.getCode() + "_" + TEMPLATE,
                                TypeUtil.objToInteger(gitlabUserId));
                        if (devopsProjectE == null) {
                            LOGGER.error(ERROR_GITLAB_GROUP_ID_SELECT);
                            return;
                        }
                        gitlabMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                                TypeUtil.objToInteger(devopsProjectE.getDevopsAppGroupId()), gitlabUserId);
                        deleteGilabRole(gitlabMemberE, devopsProjectE, gitlabUserId, false);
                    }
                });
    }

    @Override
    public void checkEnvProject(DevopsEnvironmentDTO devopsEnvironmentDTO, UserAttrDTO userAttrDTO) {
        DevopsProjectVO devopsProjectE = devopsProjectRepository
                .queryDevopsProject(devopsEnvironmentDTO.getProjectE().getId());
        if (devopsEnvironmentDTO.getGitlabEnvProjectId() == null) {
            throw new CommonException("error.env.project.not.exist");
        }
        GitlabMemberE groupMemberE = gitlabGroupMemberRepository
                .getUserMemberByUserId(TypeUtil.objToInteger(devopsProjectE.getDevopsEnvGroupId()),
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        if (groupMemberE != null && groupMemberE.getAccessLevel() == AccessLevel.OWNER.toValue()) {
            return;
        }
        GitlabMemberE newGroupMemberE = gitlabProjectRepository.getProjectMember(
                TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        if (newGroupMemberE == null || (newGroupMemberE.getAccessLevel() != AccessLevel.MASTER.toValue())) {
            throw new CommonException("error.user.not.env.pro.owner");
        }
    }

    @Override
    public MemberDTO queryByUserId(Integer groupId, Integer userId) {
        return gitlabServiceClient.queryGroupMember(groupId, userId).getBody();
    }

    @Override
    public void delete(Integer groupId, Integer userId) {
        gitlabServiceClient.deleteMember(groupId, userId);
    }

    @Override
    public int create(Integer groupId, MemberDTO memberDTO) {
        return gitlabServiceClient.createGroupMember(groupId, memberDTO).getStatusCodeValue();
    }

    @Override
    public void update(Integer groupId, MemberDTO memberDTO) {
        gitlabServiceClient.updateGroupMember(groupId, memberDTO);
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
        UserAttrE userAttrE = userAttrRepository.baseQueryById(userId);
        if (userAttrE == null) {
            throw new CommonException("The user you want to assign a role to is not created successfully!");
        }
        Integer gitlabUserId = TypeUtil.objToInteger(userAttrE.getGitlabUserId());
        DevopsProjectVO devopsProjectE;
        GitlabMemberE groupMemberE;
        Integer[] roles = {
                memberHelper.getProjectDevelopAccessLevel().toValue(),
                memberHelper.getProjectOwnerAccessLevel().toValue(),
                memberHelper.getOrganizationAccessLevel().toValue()};
        AccessLevel accessLevel = AccessLevel.forValue(Collections.max(Arrays.asList(roles)));
        // 如果当前iam用户只有项目成员的权限
        if (AccessLevel.DEVELOPER.equals(accessLevel)) {
            // 查看是不是由项目所有者改为项目成员
            devopsProjectE = devopsProjectRepository.baseQueryByProjectId(resourceId);
            groupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                    TypeUtil.objToInteger(devopsProjectE.getDevopsAppGroupId()),
                    (TypeUtil.objToInteger(userAttrE.getGitlabUserId())));
            if (groupMemberE != null && AccessLevel.OWNER.toValue() == (groupMemberE.getAccessLevel())) {
                deleteGilabRole(groupMemberE, devopsProjectE, gitlabUserId, false);
            }
            groupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                    TypeUtil.objToInteger(devopsProjectE.getDevopsEnvGroupId()),
                    (TypeUtil.objToInteger(userAttrE.getGitlabUserId())));
            if (groupMemberE != null && AccessLevel.OWNER.toValue() == (groupMemberE.getAccessLevel())) {
                deleteGilabRole(groupMemberE, devopsProjectE, gitlabUserId, true);
            }
            // 为当前项目下所有跳过权限检查的应用加上gitlab用户权限
            List<Integer> gitlabProjectIds = applicationRepository.listByProjectIdAndSkipCheck(resourceId).stream()
                    .filter(e -> e.getGitlabProjectE() != null)
                    .map(e -> e.getGitlabProjectE().getId()).collect(Collectors.toList());
            gitlabProjectIds.forEach(e -> {
                GitlabProjectDTO gitlabProjectDO = new GitlabProjectDTO();
                try {
                    gitlabProjectDO = gitlabRepository.getProjectById(e);
                } catch (CommonException exception) {
                    LOGGER.info("project not found");
                }
                if (gitlabProjectDO.getId() != null) {
                    GitlabMemberE gitlabMemberE = gitlabProjectRepository.getProjectMember(e, gitlabUserId);
                    if (gitlabMemberE == null || gitlabMemberE.getId() == null) {
                        gitlabRepository.addMemberIntoProject(e, new MemberVO(gitlabUserId, 30, ""));
                    }
                }
            });
        } else if (AccessLevel.OWNER.equals(accessLevel)) {
            if (resourceType.equals(PROJECT)) {
                try {
                    // 删除用户时同时清除gitlab的权限
                    List<Integer> gitlabProjectIds = applicationRepository
                            .listByProjectId(resourceId).stream().filter(e -> e.getGitlabProjectE() != null)
                            .map(e -> e.getGitlabProjectE().getId()).map(TypeUtil::objToInteger)
                            .collect(Collectors.toList());
                    gitlabProjectIds.forEach(e -> {
                        GitlabMemberE memberE = gitlabProjectRepository.getProjectMember(e, gitlabUserId);
                        if (memberE != null && memberE.getId() != null) {
                            gitlabRepository.removeMemberFromProject(e, gitlabUserId);
                        }
                    });
                    // 给gitlab应用组分配owner角色
                    devopsProjectE = devopsProjectRepository.baseQueryByProjectId(resourceId);
                    groupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                            TypeUtil.objToInteger(devopsProjectE.getDevopsAppGroupId()),
                            (TypeUtil.objToInteger(userAttrE.getGitlabUserId())));
                    addOrUpdateGilabRole(accessLevel, groupMemberE,
                            TypeUtil.objToInteger(devopsProjectE.getDevopsAppGroupId()), userAttrE);

                    //给gitlab环境组分配owner角色
                    groupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                            TypeUtil.objToInteger(devopsProjectE.getDevopsEnvGroupId()),
                            (TypeUtil.objToInteger(userAttrE.getGitlabUserId())));
                    addOrUpdateGilabRole(accessLevel, groupMemberE,
                            TypeUtil.objToInteger(devopsProjectE.getDevopsEnvGroupId()), userAttrE);

                } catch (Exception e) {
                    LOGGER.info(ERROR_GITLAB_GROUP_ID_SELECT);
                }
            } else {
                //给组织对应的模板库分配owner角色
                OrganizationVO organization = iamRepository.queryOrganizationById(resourceId);
                devopsProjectE = gitlabRepository.queryGroupByName(
                        organization.getCode() + "_" + TEMPLATE,
                        TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
                if (devopsProjectE == null) {
                    LOGGER.info(ERROR_GITLAB_GROUP_ID_SELECT);
                    return;
                }
                groupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                        TypeUtil.objToInteger(devopsProjectE.getDevopsAppGroupId()),
                        (TypeUtil.objToInteger(userAttrE.getGitlabUserId())));
                addOrUpdateGilabRole(accessLevel, groupMemberE,
                        TypeUtil.objToInteger(devopsProjectE.getDevopsAppGroupId()), userAttrE);
            }
        }
    }

    private void addOrUpdateGilabRole(AccessLevel level, GitlabMemberE groupMemberE, Integer groupId,
                                      UserAttrE userAttrE) {
        // 增删改用户
        switch (level) {
            case NONE:
                if (groupMemberE != null) {
                    gitlabGroupMemberRepository
                            .deleteMember(groupId, (TypeUtil.objToInteger(userAttrE.getGitlabUserId())));
                }
                break;
            case DEVELOPER:
            case MASTER:
            case OWNER:
                RequestMemberDO requestMember = new RequestMemberDO();
                requestMember.setUserId((TypeUtil.objToInteger(userAttrE.getGitlabUserId())));
                requestMember.setAccessLevel(level.toValue());
                requestMember.setExpiresAt("");
                if (groupMemberE == null) {
                    gitlabGroupMemberRepository.insertMember(groupId, requestMember);
                } else {
                    if (!Objects.equals(requestMember.getAccessLevel(), groupMemberE.getAccessLevel())) {
                        gitlabGroupMemberRepository.updateMember(groupId, requestMember);
                    }
                }
                break;
            default:
                LOGGER.error("error.gitlab.member.level");
                break;
        }
    }

    private void deleteGilabRole(GitlabMemberE groupMemberE, DevopsProjectVO devopsProjectE,
                                 Integer userId, Boolean isEnvDelete) {
        if (groupMemberE != null) {
            gitlabGroupMemberRepository.deleteMember(
                    isEnvDelete ? TypeUtil.objToInteger(devopsProjectE.getDevopsEnvGroupId())
                            : TypeUtil.objToInteger(devopsProjectE.getDevopsAppGroupId()), userId);
        }
    }
}
