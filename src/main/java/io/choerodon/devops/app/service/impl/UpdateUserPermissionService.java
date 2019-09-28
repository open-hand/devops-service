package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Objects;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
import io.choerodon.devops.infra.util.TypeUtil;


/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 16:07
 * Description:
 */
public abstract class UpdateUserPermissionService {

    private GitlabServiceClientOperator gitlabServiceClientOperator;
    private UserAttrService userAttrService;
    private AppServiceMapper appServiceMapper;
    private DevopsEnvironmentMapper devopsEnvironmentMapper;

    protected UpdateUserPermissionService() {
        this.gitlabServiceClientOperator = ApplicationContextHelper.getSpringFactory().getBean(GitlabServiceClientOperator.class);
        this.userAttrService = ApplicationContextHelper.getSpringFactory().getBean(UserAttrService.class);
        this.appServiceMapper = ApplicationContextHelper.getSpringFactory().getBean(AppServiceMapper.class);
        this.devopsEnvironmentMapper = ApplicationContextHelper.getSpringFactory().getBean(DevopsEnvironmentMapper.class);
    }

    protected UpdateUserPermissionService(GitlabServiceClientOperator gitlabServiceClientOperator,
                                          UserAttrService userAttrService,
                                          AppServiceMapper appServiceMapper,
                                          DevopsEnvironmentMapper devopsEnvironmentMapper) {
        this.gitlabServiceClientOperator = gitlabServiceClientOperator;
        this.userAttrService = userAttrService;
        this.appServiceMapper = appServiceMapper;
        this.devopsEnvironmentMapper = devopsEnvironmentMapper;
    }

    public abstract Boolean updateUserPermission(Long projectId, Long id, List<Long> userIds, Integer option);

    public void updateGitlabUserPermission(String type, Integer gitlabGroupId, Integer gitlabProjectId, List<Integer> addGitlabUserIds,
                                           List<Integer> deleteGitlabUserIds) {
        addGitlabUserIds.forEach(e -> {
            MemberDTO memberDTO = gitlabServiceClientOperator.queryGroupMember(gitlabGroupId, TypeUtil.objToInteger(e));
            if (memberDTO != null) {
                gitlabServiceClientOperator.deleteGroupMember(gitlabGroupId, TypeUtil.objToInteger(e));
                UserAttrDTO userAttrE = userAttrService.baseQueryByGitlabUserId(TypeUtil.objToLong(e));
                List<Long> gitlabProjectIds = type.equals("env") ?
                        devopsEnvironmentMapper.listGitlabProjectIdByEnvPermission(TypeUtil.objToLong(gitlabGroupId), userAttrE.getIamUserId())
                        : appServiceMapper.listGitlabProjectIdByAppPermission(TypeUtil.objToLong(gitlabGroupId), userAttrE.getIamUserId());
                if (gitlabProjectIds != null && !gitlabProjectIds.isEmpty()) {
                    gitlabProjectIds.stream().filter(Objects::nonNull).forEach(aLong -> addGitlabMember(type, TypeUtil.objToInteger(aLong), TypeUtil.objToInteger(userAttrE.getGitlabUserId())));
                }
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
        if (projectMember.getId() != null) {
            gitlabServiceClientOperator.deleteProjectMember(gitlabProjectId, userId);
        }
    }
}
