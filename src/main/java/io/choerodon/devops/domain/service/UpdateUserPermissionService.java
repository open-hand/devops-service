package io.choerodon.devops.domain.service;

import java.util.List;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.api.dto.gitlab.MemberDTO;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabMemberE;
import io.choerodon.devops.domain.application.repository.GitlabProjectRepository;
import io.choerodon.devops.domain.application.repository.GitlabRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;

/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 16:07
 * Description:
 */
public abstract class UpdateUserPermissionService {
    private GitlabProjectRepository gitlabProjectRepository;
    private GitlabRepository gitlabRepository;
    private UserAttrRepository userAttrRepository;

    protected UpdateUserPermissionService() {
        this.gitlabProjectRepository = ApplicationContextHelper.getSpringFactory()
                .getBean(GitlabProjectRepository.class);
        this.gitlabRepository = ApplicationContextHelper.getSpringFactory().getBean(GitlabRepository.class);
        this.userAttrRepository = ApplicationContextHelper.getSpringFactory().getBean(UserAttrRepository.class);
    }

    public abstract Boolean updateUserPermission(Long id, List<Long> userIds);

    protected void updateGitlabUserPermission(Long gitlabProjectId, List<Long> addUserIds, List<Long> deleteUserIds) {
        addUserIds.forEach(e -> {
            Integer gitlabUserId = TypeUtil.objToInteger(userAttrRepository.queryById(e).getGitlabUserId());
            addGitlabMember(TypeUtil.objToInteger(gitlabProjectId), gitlabUserId);
        });
        deleteUserIds.forEach(e -> {
            Integer gitlabUserId = TypeUtil.objToInteger(userAttrRepository.queryById(e).getGitlabUserId());
            deleteGitlabMember(TypeUtil.objToInteger(gitlabProjectId), gitlabUserId);
        });
    }

    private void addGitlabMember(Integer gitlabProjectId, Integer userId) {
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setUserId(userId);
        memberDTO.setAccessLevel(40);
        memberDTO.setExpiresAt("");
        gitlabRepository.addMemberIntoProject(gitlabProjectId, memberDTO);
    }

    private void deleteGitlabMember(Integer gitlabProjectId, Integer userId) {
        // permission为0的先查看在gitlab那边有没有权限，如果有，则删除gitlab权限
        GitlabMemberE gitlabMemberE = gitlabProjectRepository
                .getProjectMember(gitlabProjectId, userId);
        if (gitlabMemberE.getId() != null) {
            gitlabRepository.removeMemberFromProject(gitlabProjectId, userId);
        }
    }
}
