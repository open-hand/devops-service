package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.GitlabGroupMemberDTO;
import io.choerodon.devops.app.service.GitlabGroupMemberService;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabMemberE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabUserE;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.MemberHelper;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.AccessLevel;
import io.choerodon.devops.infra.dataobject.gitlab.RequestMemberDO;

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

    @Override
    public void createGitlabGroupMemberRole(List<GitlabGroupMemberDTO> gitlabGroupMemberDTOList) {
        gitlabGroupMemberDTOList.stream()
                .filter(gitlabGroupMemberDTO -> !gitlabGroupMemberDTO.getResourceType().equals(SITE))
                .forEach(gitlabGroupMemberDTO -> {
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
                });
    }

    @Override
    public void deleteGitlabGroupMemberRole(List<GitlabGroupMemberDTO> gitlabGroupMemberDTOList) {
        gitlabGroupMemberDTOList.stream()
                .filter(gitlabGroupMemberDTO -> !gitlabGroupMemberDTO.getResourceType().equals(SITE))
                .forEach(gitlabGroupMemberDTO -> {
                    UserAttrE userAttrE = userAttrRepository.queryById(gitlabGroupMemberDTO.getUserId());
                    Integer userId = TypeUtil.objToInteger(userAttrE.getGitlabUserId());
                    GitlabUserE gitlabUserE = gitlabUserRepository.getGitlabUserByUserId(
                            TypeUtil.objToInteger(userId));
                    if (gitlabUserE == null) {
                        LOGGER.error("error.gitlab.username.select");
                        return;
                    }
                    GitlabGroupE gitlabGroupE;
                    GitlabMemberE gitlabMemberE;
                    if (PROJECT.equals(gitlabGroupMemberDTO.getResourceType())) {
                        gitlabGroupE = devopsProjectRepository.queryDevopsProject(gitlabGroupMemberDTO.getResourceId());
                        gitlabMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                                TypeUtil.objToInteger(gitlabGroupE.getDevopsAppGroupId()),
                                userId);
                        deleteGilabRole(gitlabMemberE, gitlabGroupE, userId, false);
                        gitlabMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                                TypeUtil.objToInteger(gitlabGroupE.getDevopsEnvGroupId()),
                                userId);
                        deleteGilabRole(gitlabMemberE, gitlabGroupE, userId, false);
                    } else {
                        Organization organization =
                                iamRepository.queryOrganizationById(gitlabGroupMemberDTO.getResourceId());
                        gitlabGroupE = gitlabRepository.queryGroupByName(
                                organization.getCode() + "_" + TEMPLATE,
                                TypeUtil.objToInteger(userId));
                        if (gitlabGroupE == null) {
                            LOGGER.error(ERROR_GITLAB_GROUP_ID_SELECT);
                            return;
                        }
                        gitlabMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                                TypeUtil.objToInteger(gitlabGroupE.getDevopsAppGroupId()),
                                userId);
                        deleteGilabRole(gitlabMemberE, gitlabGroupE, userId, false);
                    }
                });
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
        UserAttrE userAttrE = userAttrRepository.queryById(userId);
        GitlabGroupE gitlabGroupE;
        GitlabMemberE groupMemberE;
        Integer[] roles = {
                memberHelper.getProjectDevelopAccessLevel().toValue(),
                memberHelper.getProjectOwnerAccessLevel().toValue(),
                memberHelper.getOrganizationAccessLevel().toValue()};
        AccessLevel accessLevel = AccessLevel.forValue(Collections.max(Arrays.asList(roles)));
        if (!accessLevel.equals(AccessLevel.NONE) && accessLevel.equals(AccessLevel.OWNER)) {
            if (resourceType.equals(PROJECT)) {
                try {
                    //给gitlab应用组分配owner角色
                    gitlabGroupE = devopsProjectRepository.queryDevopsProject(resourceId);
                    groupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                            TypeUtil.objToInteger(gitlabGroupE.getDevopsAppGroupId()),
                            (TypeUtil.objToInteger(userAttrE.getGitlabUserId())));
                    addOrUpdateGilabRole(accessLevel, groupMemberE,
                            TypeUtil.objToInteger(gitlabGroupE.getDevopsAppGroupId()), userAttrE);
                    if (accessLevel.equals(AccessLevel.OWNER)) {
                        //给gitlab环境组分配owner角色
                        groupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                                TypeUtil.objToInteger(gitlabGroupE.getDevopsEnvGroupId()),
                                (TypeUtil.objToInteger(userAttrE.getGitlabUserId())));
                        addOrUpdateGilabRole(accessLevel, groupMemberE,
                                TypeUtil.objToInteger(gitlabGroupE.getDevopsEnvGroupId()), userAttrE);
                    }
                } catch (Exception e) {
                    LOGGER.info(ERROR_GITLAB_GROUP_ID_SELECT);
                }
            } else {
                //给组织对应的模板库分配owner角色
                Organization organization = iamRepository.queryOrganizationById(resourceId);
                gitlabGroupE = gitlabRepository.queryGroupByName(
                        organization.getCode() + "_" + TEMPLATE,
                        TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
                if (gitlabGroupE == null) {
                    LOGGER.info(ERROR_GITLAB_GROUP_ID_SELECT);
                    return;
                }
                groupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                        TypeUtil.objToInteger(gitlabGroupE.getDevopsAppGroupId()),
                        (TypeUtil.objToInteger(userAttrE.getGitlabUserId())));
                addOrUpdateGilabRole(accessLevel, groupMemberE,
                        TypeUtil.objToInteger(gitlabGroupE.getDevopsAppGroupId()), userAttrE);
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
                    gitlabGroupMemberRepository.updateMember(groupId, requestMember);
                }
                break;
            default:
                LOGGER.error("error.gitlab.member.level");
                break;
        }
    }

    private void deleteGilabRole(GitlabMemberE groupMemberE, GitlabGroupE gitlabGroupE,
                                 Integer userId, Boolean isEnvDelete) {
        if (groupMemberE != null) {
            gitlabGroupMemberRepository.deleteMember(
                    isEnvDelete ? TypeUtil.objToInteger(gitlabGroupE.getDevopsEnvGroupId())
                            : TypeUtil.objToInteger(gitlabGroupE.getDevopsAppGroupId()), userId);
        }
    }

    @Override
    public void checkEnvProject(DevopsEnvironmentE devopsEnvironmentE, UserAttrE userAttrE) {
        GitlabGroupE gitlabGroupE = devopsProjectRepository
                .queryDevopsProject(devopsEnvironmentE.getProjectE().getId());
        if (gitlabGroupE == null) {
            throw new CommonException("error.group.not.sync");
        }
        if (devopsEnvironmentE.getGitlabEnvProjectId() == null) {
            throw new CommonException("error.env.project.not.exist");
        }
        GitlabMemberE groupMemberE = gitlabGroupMemberRepository
                .getUserMemberByUserId(TypeUtil.objToInteger(gitlabGroupE.getDevopsEnvGroupId()),
                        TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        if (groupMemberE != null && groupMemberE.getAccessLevel() == AccessLevel.OWNER.toValue()) {
            return;
        }
        GitlabMemberE newGroupMemberE = gitlabProjectRepository.getProjectMember(
                TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
                TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        if (newGroupMemberE == null || (newGroupMemberE.getAccessLevel() != AccessLevel.MASTER.toValue())) {
            throw new CommonException("error.user.not.env.pro.owner");
        }
    }
}
