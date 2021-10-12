package io.choerodon.devops.app.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.hrdsCode.MemberPrivilegeViewDTO;
import io.choerodon.devops.app.service.CheckGitlabAccessLevelService;
import io.choerodon.devops.app.service.PermissionHelper;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.AccessLevel;
import io.choerodon.devops.infra.enums.AppServiceEvent;
import io.choerodon.devops.infra.exception.GitlabAccessInvalidException;
import io.choerodon.devops.infra.feign.HrdsCodeRepoClient;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;

/**
 * @author scp
 * @date 2020/6/11
 * @description
 */
@Service
public class CheckGitlabAccessLevelServiceImpl implements CheckGitlabAccessLevelService {
    private final static String EMPTY_GITLAB_ACCESS_LEVEL = "error.empty.gitlab.access.level";
    @Autowired
    private HrdsCodeRepoClient hrdsCodeRepoClient;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private PermissionHelper permissionHelper;
    @Autowired
    private AppServiceService appServiceService;

    @Override
    public void checkGitlabPermission(Long projectId, Long appServiceId, AppServiceEvent appServiceEvent) {
        Long userId = DetailsHelper.getUserDetails().getUserId();
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        // 外部仓库不校验权限
        if (appServiceDTO.getExternalConfigId() != null) {
            return;
        }
        if (!permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, userId)) {
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);

            List<MemberPrivilegeViewDTO> viewDTOList = hrdsCodeRepoClient.selfPrivilege(projectDTO.getOrganizationId(), projectId, Collections.singleton(appServiceId)).getBody();
            if (CollectionUtils.isEmpty(viewDTOList) || viewDTOList.get(0).getAccessLevel() == null) {
                throw new CommonException(EMPTY_GITLAB_ACCESS_LEVEL);
            }
            Optional<Integer> max = viewDTOList.stream().map(MemberPrivilegeViewDTO::getAccessLevel).collect(Collectors.toSet()).stream().max(Integer::compare);
            if (max.isPresent()) {
                Integer maxAccessLevel = max.get();
                if (maxAccessLevel <= appServiceEvent.getAccessLevel()) {
                    throw new GitlabAccessInvalidException("error.gitlab.access.level", AccessLevel.getAccessLevelName(maxAccessLevel));
                }
            } else {
                throw new CommonException(EMPTY_GITLAB_ACCESS_LEVEL);
            }
        }
    }
}
