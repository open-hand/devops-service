package io.choerodon.devops.app.service.impl;

import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.CiVariableVO;
import io.choerodon.devops.api.vo.UserAttrVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsCiVariableService;
import io.choerodon.devops.app.service.ProjectService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DevopsCiVariableServiceImpl implements DevopsCiVariableService {
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserAttrService userAttrService;

    @Autowired
    private AppServiceService appServiceService;

    @Override
    public List<CiVariableVO> listGlobalVariable(Long projectId) {
        DevopsProjectDTO devopsProjectDTO = projectService.queryById(projectId);
        UserAttrVO userAttrVO = userAttrService.queryByUserId(DetailsHelper.getUserDetails().getUserId());
        return gitlabServiceClientOperator.listProjectVariable(devopsProjectDTO.getDevopsAppGroupId().intValue(), userAttrVO.getGitlabUserId().intValue());
    }

    @Override
    public List<CiVariableVO> listAppServiceVariable(Long projectId, Long appServiceId) {
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        UserAttrVO userAttrVO = userAttrService.queryByUserId(DetailsHelper.getUserDetails().getUserId());
        return gitlabServiceClientOperator.listAppServiceVariable(appServiceDTO.getGitlabProjectId(), userAttrVO.getGitlabUserId().intValue());
    }
}
