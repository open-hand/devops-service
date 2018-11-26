package io.choerodon.devops.domain.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabMemberE;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.service.UpdateUserPermissionService;
import io.choerodon.devops.infra.common.util.TypeUtil;

/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 16:08
 * Description:
 */
public class UpdateAppUserPermissionServiceImpl extends UpdateUserPermissionService {

    private ApplicationRepository applicationRepository;
    private AppUserPermissionRepository appUserPermissionRepository;
    private IamRepository iamRepository;
    private UserAttrRepository userAttrRepository;
    private GitlabProjectRepository gitlabProjectRepository;

    public UpdateAppUserPermissionServiceImpl() {
        this.applicationRepository = ApplicationContextHelper.getSpringFactory().getBean(ApplicationRepository.class);
        this.appUserPermissionRepository = ApplicationContextHelper.getSpringFactory()
                .getBean(AppUserPermissionRepository.class);
        this.iamRepository = ApplicationContextHelper.getSpringFactory().getBean(IamRepository.class);
        this.userAttrRepository = ApplicationContextHelper.getSpringFactory().getBean(UserAttrRepository.class);
        this.gitlabProjectRepository = ApplicationContextHelper.getSpringFactory()
                .getBean(GitlabProjectRepository.class);
    }

    @Override
    public Boolean updateUserPermission(Long projectId, Long appId, List<Long> userIds, Integer option) {

        List<Integer> allMemberGitlabIdsWithoutOwner;
        List<Integer> addGitlabUserIds;
        List<Integer> deleteGitlabUserIds;
        List<Integer> updateGitlabUserIds;

        Integer gitlabProjectId = applicationRepository.query(appId).getGitlabProjectE().getId();
        switch (option) {
            // 原来跳过，现在不跳过，需要更新权限表，而且需要去掉原来gitlab中的权限
            case 1:
                // devops
                userIds.forEach(e -> appUserPermissionRepository.create(e, appId));

                // gitlab
                updateGitlabUserIds = userAttrRepository.listByUserIds(userIds)
                        .stream().map(e -> TypeUtil.objToInteger(e.getGitlabUserId())).collect(Collectors.toList());
                // 获取项目下所有项目成员的gitlabUserIds，过滤掉项目所有者
                allMemberGitlabIdsWithoutOwner = getAllGitlabMemberWithoutOwner(projectId);

                addGitlabUserIds = new ArrayList<>(updateGitlabUserIds);
                addGitlabUserIds.removeAll(allMemberGitlabIdsWithoutOwner);

                deleteGitlabUserIds = new ArrayList<>(allMemberGitlabIdsWithoutOwner);
                deleteGitlabUserIds.removeAll(updateGitlabUserIds);

                super.updateGitlabUserPermission(gitlabProjectId, addGitlabUserIds, deleteGitlabUserIds);
                return true;
            // 原来不跳过，现在跳过，需要删除权限表中的所有人，然后把项目下所有项目成员加入gitlab权限
            case 2:
                // 获取项目下所有项目成员的gitlabUserIds，过滤掉项目所有者
                allMemberGitlabIdsWithoutOwner = getAllGitlabMemberWithoutOwner(projectId);

                addGitlabUserIds = allMemberGitlabIdsWithoutOwner.stream()
                        .filter(e -> !gitlabProjectRepository.getAllMemberByProjectId(gitlabProjectId).stream()
                                .map(GitlabMemberE::getId).collect(Collectors.toList()).contains(e))
                        .collect(Collectors.toList());

                super.updateGitlabUserPermission(gitlabProjectId, addGitlabUserIds, new ArrayList<>());
                return true;
            // 原来不跳过，现在也不跳过，需要更新权限表
            case 3:
                // devops
                appUserPermissionRepository.deleteByAppId(appId);
                userIds.forEach(e -> appUserPermissionRepository.create(e, appId));

                // gitlab
                updateGitlabUserIds = userAttrRepository.listByUserIds(userIds).stream()
                        .map(e -> TypeUtil.objToInteger(e.getGitlabUserId())).collect(Collectors.toList());
                List<Integer> currentGitlabUserIds = gitlabProjectRepository.getAllMemberByProjectId(gitlabProjectId)
                        .stream().map(GitlabMemberE::getId).collect(Collectors.toList());

                addGitlabUserIds = new ArrayList<>(updateGitlabUserIds);
                addGitlabUserIds.removeAll(currentGitlabUserIds);

                deleteGitlabUserIds = new ArrayList<>(currentGitlabUserIds);
                deleteGitlabUserIds.removeAll(updateGitlabUserIds);

                super.updateGitlabUserPermission(gitlabProjectId, addGitlabUserIds, deleteGitlabUserIds);
                return true;
            default:
                return true;
        }
    }

    // 获取iam项目下所有的项目成员的gitlabUserId，过滤掉项目所有者
    private List<Integer> getAllGitlabMemberWithoutOwner(Long projectId) {
        return userAttrRepository.listByUserIds(iamRepository.getAllMemberIdsWithoutOwner(projectId)).stream()
                .map(UserAttrE::getGitlabUserId).collect(Collectors.toList()).stream()
                .map(TypeUtil::objToInteger).collect(Collectors.toList());
    }
}
