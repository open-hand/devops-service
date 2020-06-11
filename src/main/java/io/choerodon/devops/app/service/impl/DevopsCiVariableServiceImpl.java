package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lihao
 */
@Service
public class DevopsCiVariableServiceImpl implements DevopsCiVariableService {
    private static final String LEVEL_PROJECT = "project";
    private static final String LEVEL_APP_SERVICE = "app";


    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserAttrService userAttrService;

    @Autowired
    private AppServiceService appServiceService;

    @Override
    public Map<String, List<CiVariableVO>> listKeys(Long projectId, Long appServiceId) {
        Map<String, List<CiVariableVO>> keys = new HashMap<>();
        List<CiVariableVO> ciVariableVOSOnProject = new ArrayList<>();
        List<CiVariableVO> ciVariableVOSOnApp = new ArrayList<>();
        UserAttrVO userAttrVO = userAttrService.queryByUserId(DetailsHelper.getUserDetails().getUserId());
        DevopsProjectDTO devopsProjectDTO = projectService.queryById(projectId);
        ciVariableVOSOnProject.addAll(gitlabServiceClientOperator.listProjectVariable(devopsProjectDTO.getDevopsAppGroupId().intValue(), userAttrVO.getGitlabUserId().intValue()));
        if (appServiceId != null) {
            AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
            ciVariableVOSOnApp.addAll(gitlabServiceClientOperator.listAppServiceVariable(appServiceDTO.getGitlabProjectId(), userAttrVO.getGitlabUserId().intValue()));
        }
        keys.put("project", eraseValue(ciVariableVOSOnProject));
        keys.put("app", eraseValue(ciVariableVOSOnApp));
        return keys;
    }

    @Override
    public List<CiVariableVO> listValues(Long projectId, String level, Long appServiceId) {
        UserAttrVO userAttrVO = userAttrService.queryByUserId(DetailsHelper.getUserDetails().getUserId());
        switch (level) {
            case LEVEL_PROJECT:
                DevopsProjectDTO devopsProjectDTO = projectService.queryById(projectId);
                return gitlabServiceClientOperator.listProjectVariable(devopsProjectDTO.getDevopsAppGroupId().intValue(), userAttrVO.getGitlabUserId().intValue());
            case LEVEL_APP_SERVICE:
                if (appServiceId == null) {
                    return null;
                }
                AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
                return gitlabServiceClientOperator.listAppServiceVariable(appServiceDTO.getGitlabProjectId(), userAttrVO.getGitlabUserId().intValue());
            default:
                throw new CommonException("error.level.error");
        }
    }

    private List<CiVariableVO> eraseValue(List<CiVariableVO> ciVariableVOList) {
        return ciVariableVOList.stream().peek(ciVariableVO -> ciVariableVO.setValue(null)).collect(Collectors.toList());
    }
}
