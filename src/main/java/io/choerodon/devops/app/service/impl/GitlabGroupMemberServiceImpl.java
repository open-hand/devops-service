package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.dto.GitlabGroupMemberDTO;
import io.choerodon.devops.app.service.GitlabGroupMemberService;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupMemberE;
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


    @Override
    public void createGitlabGroupMemberRole(List<GitlabGroupMemberDTO> gitlabGroupMemberDTOList) {
        gitlabGroupMemberDTOList.parallelStream()
                .filter(gitlabGroupMemberDTO -> !gitlabGroupMemberDTO.getResourceType().equals(SITE))
                .forEach(gitlabGroupMemberDTO -> {
                    List<String> userMemberRoleList = gitlabGroupMemberDTO.getRoleLabels();
                    if (userMemberRoleList.isEmpty()) {
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
        gitlabGroupMemberDTOList.parallelStream()
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
                    GitlabGroupMemberE groupMemberE;
                    if (PROJECT.equals(gitlabGroupMemberDTO.getResourceType())) {
                        gitlabGroupE = devopsProjectRepository.queryDevopsProject(gitlabGroupMemberDTO.getResourceId());
                        groupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                                gitlabGroupE.getEnvGroupId(),
                                userId);
                        deleteGilabRole(groupMemberE, gitlabGroupE, userId, true);
                        groupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                                gitlabGroupE.getGitlabGroupId(),
                                userId);
                        deleteGilabRole(groupMemberE, gitlabGroupE, userId, false);
                    } else {
                        Organization organization =
                                iamRepository.queryOrganizationById(gitlabGroupMemberDTO.getResourceId());
                        gitlabGroupE = gitlabRepository.queryGroupByName(
                                organization.getCode() + "_" + TEMPLATE,
                                TypeUtil.objToInteger(userId));
                        if (gitlabGroupE == null) {
                            LOGGER.error("error.gitlab.groupId.select");
                            return;
                        }
                        groupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                                gitlabGroupE.getGitlabGroupId(),
                                userId);
                        deleteGilabRole(groupMemberE, gitlabGroupE, userId, false);
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
    public void operation(Long resourceId, String resourceType, MemberHelper memberHelper, Long userId) {
        UserAttrE userAttrE = userAttrRepository.queryById(userId);
        GitlabGroupE gitlabGroupE;
        GitlabGroupMemberE groupMemberE;
        Integer[] roles = {
                memberHelper.getProjectDevelopAccessLevel().toValue(),
                memberHelper.getProjectOwnerAccessLevel().toValue()};
        AccessLevel accessLevel = AccessLevel.forValue(Collections.max(Arrays.asList(roles)));
        if (!memberHelper.isDeploy()) {
            if (resourceType.equals(PROJECT)) {
                try {
                    gitlabGroupE = devopsProjectRepository.queryDevopsProject(resourceId);
                } catch (Exception e) {
                    LOGGER.info("error.gitlab.groupId.select");
                    return;
                }
            } else {
                Organization organization = iamRepository.queryOrganizationById(resourceId);
                gitlabGroupE = gitlabRepository.queryGroupByName(
                        organization.getCode() + "_" + TEMPLATE,
                        TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
                if (gitlabGroupE == null) {
                    LOGGER.info("error.gitlab.groupId.select");
                    return;
                }
            }
            groupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                    gitlabGroupE.getGitlabGroupId(),
                    (TypeUtil.objToInteger(userAttrE.getGitlabUserId())));
            addOrUpdateGilabRole(accessLevel, groupMemberE, gitlabGroupE.getGitlabGroupId(), userAttrE);
            groupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                    gitlabGroupE.getEnvGroupId(),
                    (TypeUtil.objToInteger(userAttrE.getGitlabUserId())));
            addOrUpdateGilabRole(accessLevel, groupMemberE, gitlabGroupE.getEnvGroupId(), userAttrE);
        } else {
            try {
                gitlabGroupE = devopsProjectRepository.queryDevopsProject(resourceId);
            } catch (Exception e) {
                LOGGER.info("error.gitlab.groupId.select");
                return;
            }
            groupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                    gitlabGroupE.getGitlabGroupId(),
                    (TypeUtil.objToInteger(userAttrE.getGitlabUserId())));
            addOrUpdateGilabRole(accessLevel, groupMemberE, gitlabGroupE.getGitlabGroupId(), userAttrE);
            groupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                    gitlabGroupE.getEnvGroupId(),
                    (TypeUtil.objToInteger(userAttrE.getGitlabUserId())));
            addOrUpdateGilabRole(
                    memberHelper.getDeployAdminAccessLevel(),
                    groupMemberE,
                    gitlabGroupE.getEnvGroupId(),
                    userAttrE);
        }
    }


    public void addOrUpdateGilabRole(AccessLevel level, GitlabGroupMemberE groupMemberE, Integer groupId, UserAttrE userAttrE) {
        // 增删改用户
        switch (level) {
            case NONE:
                if (groupMemberE != null) {
                    gitlabGroupMemberRepository.deleteMember(
                            groupId,
                            (TypeUtil.objToInteger(userAttrE.getGitlabUserId())));
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
                    gitlabGroupMemberRepository.insertMember(
                            groupId,
                            requestMember);
                } else {
                    gitlabGroupMemberRepository.updateMember(
                            groupId,
                            requestMember);
                }
                break;
            default:
                LOGGER.error("error.gitlab.member.level");
                break;
        }
    }


    public void deleteGilabRole(GitlabGroupMemberE groupMemberE, GitlabGroupE gitlabGroupE,
                                Integer userId, Boolean isEnvDelete) {
        if (groupMemberE != null) {
            gitlabGroupMemberRepository.deleteMember(
                    isEnvDelete ? gitlabGroupE.getEnvGroupId() : gitlabGroupE.getGitlabGroupId(),
                    userId);
        }
    }
}
