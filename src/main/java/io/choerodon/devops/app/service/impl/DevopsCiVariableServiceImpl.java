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

import java.util.List;
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
    public List<CiVariableVO> listKeys(Long projectId, String level, Long appServiceId) {
        UserAttrVO userAttrVO = userAttrService.queryByUserId(DetailsHelper.getUserDetails().getUserId());
        switch (level) {
            case LEVEL_PROJECT:
                DevopsProjectDTO devopsProjectDTO = projectService.queryById(projectId);
                return eraseValue(gitlabServiceClientOperator.listProjectVariable(devopsProjectDTO.getDevopsAppGroupId().intValue(), userAttrVO.getGitlabUserId().intValue()));
            case LEVEL_APP_SERVICE:
                if (appServiceId == null) {
                    return null;
                }
                AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
                return eraseValue(gitlabServiceClientOperator.listAppServiceVariable(appServiceDTO.getGitlabProjectId(), userAttrVO.getGitlabUserId().intValue()));
            default:
                throw new CommonException("error.level.error");
        }
    }

    @Override
    public List<CiVariableVO> listValues(Long projectId, String level, Long appServiceId, List<String> keys) {
        List<CiVariableVO> ciVariableVOList;

        UserAttrVO userAttrVO = userAttrService.queryByUserId(DetailsHelper.getUserDetails().getUserId());
        switch (level) {
            case LEVEL_PROJECT:
                DevopsProjectDTO devopsProjectDTO = projectService.queryById(projectId);
                ciVariableVOList = gitlabServiceClientOperator.listProjectVariable(devopsProjectDTO.getDevopsAppGroupId().intValue(), userAttrVO.getGitlabUserId().intValue());
                break;
            case LEVEL_APP_SERVICE:
                if (appServiceId == null) {
                    return null;
                }
                AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
                ciVariableVOList = gitlabServiceClientOperator.listAppServiceVariable(appServiceDTO.getGitlabProjectId(), userAttrVO.getGitlabUserId().intValue());
                break;
            default:
                throw new CommonException("error.level.error");
        }
        if (ciVariableVOList == null) {
            throw new CommonException("error.level.error");
        }
        return ciVariableVOList.stream()
                .filter(ciVariableVO -> keys.contains(ciVariableVO.getKey()))
                .collect(Collectors.toList());
    }

    private List<CiVariableVO> eraseValue(List<CiVariableVO> ciVariableVOList) {
        return ciVariableVOList.stream().peek(ciVariableVO -> ciVariableVO.setValue(null)).collect(Collectors.toList());
    }
}
