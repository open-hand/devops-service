package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.CiVariableVO;
import io.choerodon.devops.api.vo.UserAttrVO;
import io.choerodon.devops.app.service.*;
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

    @Autowired
    private PermissionHelper permissionHelper;

    @Override
    public Map<String, List<CiVariableVO>> listKeys(Long projectId, Long appServiceId) {
        Map<String, List<CiVariableVO>> keys = new HashMap<>();
        List<CiVariableVO> ciVariableVOListOnProject = new ArrayList<>();
        List<CiVariableVO> ciVariableVOListOnApp = new ArrayList<>();
        ciVariableVOListOnProject.addAll(listKeysOnProject(projectId));
        if (appServiceId != null) {
            ciVariableVOListOnApp.addAll(listKeysOnApp(appServiceId));
        }
        keys.put(LEVEL_PROJECT, eraseValue(ciVariableVOListOnProject));
        keys.put(LEVEL_APP_SERVICE, eraseValue(ciVariableVOListOnApp));
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

    @Override
    public void save(Long projectId, String level, Long appServiceId, List<CiVariableVO> ciVariableVOList) {
        List<String> keysToDelete;
        List<String> newKeys = ciVariableVOList.stream().map(CiVariableVO::getKey).collect(Collectors.toList());
        switch (level) {
            case LEVEL_PROJECT:
                List<CiVariableVO> existCiVariablesOnProject = listKeysOnProject(projectId);
                keysToDelete = existCiVariablesOnProject.stream().filter(ciVariableVO -> !newKeys.contains(ciVariableVO.getKey()))
                        .map(CiVariableVO::getKey)
                        .collect(Collectors.toList());
                performUpdate(projectId, level, appServiceId, keysToDelete, ciVariableVOList);
                break;
            case LEVEL_APP_SERVICE:
                permissionHelper.checkAppServiceBelongToProject(projectId, appServiceId);
                List<CiVariableVO> existCiVariablesOnApp = listKeysOnApp(appServiceId);
                keysToDelete = existCiVariablesOnApp.stream().filter(ciVariableVO -> !newKeys.contains(ciVariableVO.getKey()))
                        .map(CiVariableVO::getKey)
                        .collect(Collectors.toList());
                performUpdate(projectId, level, appServiceId, keysToDelete, ciVariableVOList);
                break;
            default:
                throw new CommonException("error.level.error");
        }
    }

    @Override
    public List<CiVariableVO> batchUpdate(Long projectId, String level, Long appServiceId, List<CiVariableVO> ciVariableVOList) {
        UserAttrVO userAttrVO = userAttrService.queryByUserId(DetailsHelper.getUserDetails().getUserId());
        switch (level) {
            case LEVEL_PROJECT:
                DevopsProjectDTO devopsProjectDTO = projectService.queryById(projectId);
                return gitlabServiceClientOperator.batchSaveGroupVariable(devopsProjectDTO.getDevopsAppGroupId().intValue(), userAttrVO.getGitlabUserId().intValue(), ciVariableVOList);
            case LEVEL_APP_SERVICE:
                if (appServiceId == null) {
                    return null;
                }
                AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
                return gitlabServiceClientOperator.batchSaveProjectVariable(appServiceDTO.getGitlabProjectId(), userAttrVO.getGitlabUserId().intValue(), ciVariableVOList);
            default:
                throw new CommonException("error.level.error");
        }
    }

    @Override
    public void batchDelete(Long projectId, String level, Long appServiceId, List<String> keys) {
        UserAttrVO userAttrVO = userAttrService.queryByUserId(DetailsHelper.getUserDetails().getUserId());
        switch (level) {
            case LEVEL_PROJECT:
                DevopsProjectDTO devopsProjectDTO = projectService.queryById(projectId);
                gitlabServiceClientOperator.batchDeleteGroupVariable(devopsProjectDTO.getDevopsAppGroupId().intValue(), userAttrVO.getGitlabUserId().intValue(), keys);
                break;
            case LEVEL_APP_SERVICE:
                if (appServiceId == null) {
                    return;
                }
                AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
                gitlabServiceClientOperator.batchDeleteProjectVariable(appServiceDTO.getGitlabProjectId(), userAttrVO.getGitlabUserId().intValue(), keys);
                break;
            default:
                throw new CommonException("error.level.error");
        }
    }

    @Override
    public List<CiVariableVO> listKeysOnProject(Long projectId) {
        UserAttrVO userAttrVO = userAttrService.queryByUserId(DetailsHelper.getUserDetails().getUserId());
        DevopsProjectDTO devopsProjectDTO = projectService.queryById(projectId);
        return gitlabServiceClientOperator.listProjectVariable(devopsProjectDTO.getDevopsAppGroupId().intValue(), userAttrVO.getGitlabUserId().intValue());
    }

    @Override
    public List<CiVariableVO> listKeysOnApp(Long appServiceId) {
        UserAttrVO userAttrVO = userAttrService.queryByUserId(DetailsHelper.getUserDetails().getUserId());
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        return gitlabServiceClientOperator.listAppServiceVariable(appServiceDTO.getGitlabProjectId(), userAttrVO.getGitlabUserId().intValue());
    }

    private void performUpdate(Long projectId, String level, Long appServiceId, List<String> keysToDelete, List<CiVariableVO> ciVariableVOList) {
        if (keysToDelete.size() != 0) {
            batchDelete(projectId, level, appServiceId, keysToDelete);
        }
        if (ciVariableVOList.size() != 0) {
            batchUpdate(projectId, level, appServiceId, ciVariableVOList);
        }
    }

    private List<CiVariableVO> eraseValue(List<CiVariableVO> ciVariableVOList) {
        return ciVariableVOList.stream().peek(ciVariableVO -> ciVariableVO.setValue(null)).collect(Collectors.toList());
    }
}
