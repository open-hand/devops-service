package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 16:08
 * Description:
 */
public class UpdateAppUserPermissionServiceImpl extends UpdateUserPermissionService {

    private AppServiceService applicationService;
    private BaseServiceClientOperator baseServiceClientOperator;
    private UserAttrService userAttrService;
    private DevopsProjectService devopsProjectService;
    private GitlabServiceClientOperator gitlabServiceClientOperator;

    public UpdateAppUserPermissionServiceImpl() {
        this.applicationService = ApplicationContextHelper.getSpringFactory().getBean(AppServiceService.class);
        this.baseServiceClientOperator = ApplicationContextHelper.getSpringFactory().getBean(BaseServiceClientOperator.class);
        this.userAttrService = ApplicationContextHelper.getSpringFactory().getBean(UserAttrService.class);
        this.devopsProjectService = ApplicationContextHelper.getSpringFactory()
                .getBean(DevopsProjectService.class);
        this.gitlabServiceClientOperator = ApplicationContextHelper.getSpringFactory()
                .getBean(GitlabServiceClientOperator.class);
    }

    // 获取iam项目下所有的项目成员的gitlabUserId，过滤掉项目所有者
    private List<Integer> getAllGitlabMemberWithoutOwner(Long projectId) {
        return userAttrService.baseListByUserIds(baseServiceClientOperator.getAllMemberIdsWithoutOwner(projectId)).stream()
                .map(UserAttrDTO::getGitlabUserId)
                .map(TypeUtil::objToInteger).collect(Collectors.toList());
    }
}
