package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
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
        for (GitlabGroupMemberDTO gitlabGroupMemberDTO : gitlabGroupMemberDTOList) {
            if (!gitlabGroupMemberDTO.getResourceType().equals(SITE)) {
                List<Integer> accessLevelList = new ArrayList<>();
                accessLevelList.add(0);
                List<String> userMemberRoleList = gitlabGroupMemberDTO.getRoleLabels();
                if (userMemberRoleList.isEmpty()) {
                    LOGGER.info("user member role is empty");
                }
                AccessLevel level = getGitlabGroupMemberRole(userMemberRoleList);

                operation(gitlabGroupMemberDTO.getResourceId(), gitlabGroupMemberDTO.getResourceType(), level,
                        gitlabGroupMemberDTO.getUserId());
            }
        }
    }

    @Override
    public void deleteGitlabGroupMemberRole(List<GitlabGroupMemberDTO> gitlabGroupMemberDTOList) {
        gitlabGroupMemberDTOList.stream()
                .filter(gitlabGroupMemberDTO -> !gitlabGroupMemberDTO.getResourceType().equals(SITE))
                .forEach(gitlabGroupMemberDTO -> {
                    GitlabUserE gitlabUserE = gitlabUserRepository.getGitlabUserByUserId(
                            TypeUtil.objToInteger(gitlabGroupMemberDTO.getUserId()));
                    if (gitlabUserE == null) {
                        LOGGER.error("error.gitlab.username.select");
                        return;
                    }
                    GitlabGroupE gitlabGroupE;
                    if (PROJECT.equals(gitlabGroupMemberDTO.getResourceType())) {
                        gitlabGroupE = devopsProjectRepository.queryDevopsProject(gitlabGroupMemberDTO.getResourceId());
                    } else {
                        Organization organization =
                                iamRepository.queryOrganizationById(gitlabGroupMemberDTO.getResourceId());
                        gitlabGroupE = gitlabRepository.queryGroupByName(
                                organization.getCode() + "_" + TEMPLATE,
                                TypeUtil.objToInteger(gitlabUserE.getId()));
                    }
                    if (gitlabGroupE.getGitlabGroupId() == null) {
                        LOGGER.error("error.gitlab.groupId.select");
                        return;
                    }

                    GitlabGroupMemberE grouoMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                            gitlabGroupE.getEnvGroupId(),
                            gitlabUserE.getId());

                    if (grouoMemberE != null) {
                        gitlabGroupMemberRepository.deleteMember(
                                gitlabGroupE.getGitlabGroupId(),
                                gitlabUserE.getId());
                    }
                });
    }

    /**
     * get AccessLevel
     *
     * @param userMemberRoleList userMemberRoleList
     */
    private AccessLevel getGitlabGroupMemberRole(List<String> userMemberRoleList) {
        List<Integer> accessLevelList = new ArrayList<>();
        accessLevelList.add(0);
        userMemberRoleList.forEach(level -> {
            AccessLevel levels = AccessLevel.forString(level.toUpperCase());
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
        return AccessLevel.forValue(Collections.max(accessLevelList));
    }

    /**
     * The user action
     *
     * @param resourceId   资源Id
     * @param resourceType 资源type
     * @param level        level
     * @param userId       userId
     */
    public void operation(Long resourceId, String resourceType, AccessLevel level, Long userId) {
        UserAttrE userAttrE = userAttrRepository.queryById(userId);
        GitlabGroupE gitlabGroupE;
        if (resourceType.equals(PROJECT)) {
            gitlabGroupE = devopsProjectRepository.queryDevopsProject(resourceId);
        } else {
            Organization organization = iamRepository.queryOrganizationById(resourceId);
            gitlabGroupE = gitlabRepository.queryGroupByName(
                    organization.getCode() + "_" + TEMPLATE,
                    TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        }
        if (gitlabGroupE.getGitlabGroupId() == null) {
            LOGGER.error("error.gitlab.groupId.select");
            return;
        }

        GitlabGroupMemberE grouoMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                gitlabGroupE.getGitlabGroupId(),
                (TypeUtil.objToInteger(userAttrE.getGitlabUserId())));

        // 增删改用户
        switch (level) {
            case NONE:
                if (grouoMemberE != null) {
                    gitlabGroupMemberRepository.deleteMember(
                            gitlabGroupE.getGitlabGroupId(),
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
                if (grouoMemberE == null) {
                    gitlabGroupMemberRepository.insertMember(
                            gitlabGroupE.getGitlabGroupId(),
                            requestMember);
                } else {
                    gitlabGroupMemberRepository.updateMember(
                            gitlabGroupE.getGitlabGroupId(),
                            requestMember);
                }
                break;
            default:
                LOGGER.error("error.gitlab.member.level");
                break;
        }
    }
}
