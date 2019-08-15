package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.util.TypeUtil;


/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 16:07
 * Description:
 */
public abstract class UpdateUserPermissionService {

    private GitlabServiceClientOperator gitlabServiceClientOperator;

    protected UpdateUserPermissionService() {
        this.gitlabServiceClientOperator = ApplicationContextHelper.getSpringFactory()
                .getBean(GitlabServiceClientOperator.class);
    }

    protected UpdateUserPermissionService(GitlabServiceClientOperator gitlabServiceClientOperator) {
        this.gitlabServiceClientOperator = gitlabServiceClientOperator;
    }

    public abstract Boolean updateUserPermission(Long projectId, Long id, List<Long> userIds, Integer option);

    public void updateGitlabUserPermission(String type, Integer gitlabGroupId, Integer gitlabProjectId, List<Integer> addGitlabUserIds,
                                           List<Integer> deleteGitlabUserIds) {
        addGitlabUserIds.forEach(e -> {
            MemberDTO memberDTO = gitlabServiceClientOperator.queryGroupMember(gitlabGroupId, TypeUtil.objToInteger(e));
            if (memberDTO != null) {
                gitlabServiceClientOperator.deleteGroupMember(gitlabGroupId, TypeUtil.objToInteger(e));
            }
            addGitlabMember(type, TypeUtil.objToInteger(gitlabProjectId), e);
        });
        deleteGitlabUserIds.forEach(e -> deleteGitlabMember(TypeUtil.objToInteger(gitlabProjectId), e));
    }

    private void addGitlabMember(String type, Integer gitlabProjectId, Integer userId) {
        MemberDTO projectMember = gitlabServiceClientOperator.getProjectMember(gitlabProjectId, userId);
        if (projectMember == null) {
            MemberDTO memberDTO = null;
            if (type.equals("env")) {
                memberDTO = new MemberDTO(userId, 40, "");
            } else {
                memberDTO = new MemberDTO(userId, 30, "");
            }
            gitlabServiceClientOperator.createProjectMember(gitlabProjectId, memberDTO);
        }
    }

    private void deleteGitlabMember(Integer gitlabProjectId, Integer userId) {
        MemberDTO projectMember = gitlabServiceClientOperator
                .getProjectMember(gitlabProjectId, userId);
        if (projectMember.getUserId() != null) {
            gitlabServiceClientOperator.deleteProjectMember(gitlabProjectId, userId);
        }
    }
}
