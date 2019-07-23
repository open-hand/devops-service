package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.kubernetes.client.models.V1Endpoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.domain.application.valueobject.DevopsCustomizeResource;
import io.choerodon.devops.infra.dto.DevopsCustomizeResourceContentDTO;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Created by Sheep on 2019/7/1.
 */

@Service
public class HandlerCustomResourceServiceImpl implements HandlerObjectFileRelationsService<DevopsCustomizeResource> {

    public static final String UPDATE = "update";
    public static final String CREATE = "create";
    private static final String GIT_SUFFIX = "/.git";
    @Autowired
    private DevopsCustomizeResourceService devopsCustomizeResourceService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private DevopsCustomizeResourceContentService devopsCustomizeResourceContentService;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;

    @Override
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceDTO> beforeSync, List<DevopsCustomizeResource> devopsCustomizeResources, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<io.choerodon.devops.infra.dto.DevopsCustomizeResourceDTO> beforeDevopsCustomResource = beforeSync.stream()
                .filter(devopsEnvFileResourceVO -> devopsEnvFileResourceVO.getResourceType().equals(ResourceType.CUSTOM.getType()))
                .map(devopsEnvFileResourceVO -> {
                    io.choerodon.devops.infra.dto.DevopsCustomizeResourceDTO devopsCustomizeResourceDTO = devopsCustomizeResourceService
                            .baseQuery(devopsEnvFileResourceVO.getResourceId());
                    if (devopsCustomizeResourceDTO == null) {
                        devopsEnvFileResourceService
                                .baseDeleteByEnvIdAndResourceId(envId, devopsEnvFileResourceVO.getResourceId(), ResourceType.CUSTOM.getType());
                        return null;
                    }
                    return devopsCustomizeResourceDTO;
                }).collect(Collectors.toList());

        //比较已存在实例和新增要处理的configMap,获取新增configMap，更新configMap，删除configMap
        List<DevopsCustomizeResource> customizeResourceDTOS = new ArrayList<>();
        List<DevopsCustomizeResource> updateDevopsCustomizeResourceDTOs = new ArrayList<>();
        devopsCustomizeResources.forEach(devopsCustomizeResourceDTO -> {
            if (beforeDevopsCustomResource.contains(devopsCustomizeResourceDTO)) {
                updateDevopsCustomizeResourceDTOs.add(devopsCustomizeResourceDTO);
                beforeDevopsCustomResource.remove(devopsCustomizeResourceDTO);
            } else {
                customizeResourceDTOS.add(devopsCustomizeResourceDTO);
            }
        });

        //新增configMap
        addDevopsCustomResource(objectPath, projectId, envId, customizeResourceDTOS, path, userId);
        //更新configMap
        updateDevopsCustomResource(objectPath, projectId, envId, updateDevopsCustomizeResourceDTOs, path, userId);
        //删除configMap,和文件对象关联关系
        beforeDevopsCustomResource.forEach(devopsCustomizeResourceE -> {
            io.choerodon.devops.infra.dto.DevopsCustomizeResourceDTO oldDevopsCustomizeResourceDTO = devopsCustomizeResourceService.queryByEnvIdAndKindAndName(envId, devopsCustomizeResourceE.getK8sKind(), devopsCustomizeResourceE.getName());
            if (oldDevopsCustomizeResourceDTO != null) {
                devopsCustomizeResourceService.deleteResourceByGitOps(oldDevopsCustomizeResourceDTO.getId());
                devopsEnvFileResourceService.baseDeleteByEnvIdAndResourceId(envId, oldDevopsCustomizeResourceDTO.getId(), ResourceType.CUSTOM.getType());
            }
        });
    }


    private void updateDevopsCustomResource(Map<String, String> objectPath, Long projectId, Long
        envId, List < DevopsCustomizeResource > devopsCustomizeResourceVOS, String path, Long userId){
            devopsCustomizeResourceVOS.stream()
                    .forEach(devopsCustomizeResourceE -> {
                        String filePath = "";
                        try {
                            filePath = objectPath.get(TypeUtil.objToString(devopsCustomizeResourceE.hashCode()));
                            io.choerodon.devops.infra.dto.DevopsCustomizeResourceDTO oldDevopsCustomizeResourceDTO = devopsCustomizeResourceService
                                    .queryByEnvIdAndKindAndName(envId, devopsCustomizeResourceE.getK8sKind(), devopsCustomizeResourceE.getName());

                            //判断自定义资源是否发生了改变
                            DevopsCustomizeResourceContentDTO devopsCustomizeResourceContentDTO = devopsCustomizeResourceContentService.baseQuery(oldDevopsCustomizeResourceDTO.getContentId());
                            Boolean isNotChange = devopsCustomizeResourceE.getDevopsCustomizeResourceContentDTO().getContent().equals(devopsCustomizeResourceContentDTO.getContent());
                            DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(oldDevopsCustomizeResourceDTO.getEnvId());

                            //发生改变走处理改变自定义资源的逻辑
                            if (!isNotChange) {
                                oldDevopsCustomizeResourceDTO.setProjectId(projectId);
                                oldDevopsCustomizeResourceDTO.setCommandId(oldDevopsCustomizeResourceDTO.getContentId());
                                devopsCustomizeResourceService.createOrUpdateResourceByGitOps(UPDATE, oldDevopsCustomizeResourceDTO, userId, envId);
                                io.choerodon.devops.infra.dto.DevopsCustomizeResourceDTO newDevopsCustomizeResourceDTO = devopsCustomizeResourceService
                                        .queryByEnvIdAndKindAndName(envId, devopsCustomizeResourceE.getK8sKind(), devopsCustomizeResourceE.getName());
                                devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(newDevopsCustomizeResourceDTO.getCommandId());
                            }

                            //没发生改变,更新commit记录，更新文件对应关系记录
                            devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                            devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
                            DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                                    .baseQueryByEnvIdAndResourceId(envId, oldDevopsCustomizeResourceDTO.getId(), ResourceType.CUSTOM.getType());
                            devopsEnvFileResourceService.updateOrCreateFileResource(objectPath,
                                    envId,
                                    devopsEnvFileResourceDTO,
                                    devopsCustomizeResourceE.hashCode(), oldDevopsCustomizeResourceDTO.getId(), ResourceType.CUSTOM.getType());

                        } catch (CommonException e) {
                            String errorCode = "";
                            if (e instanceof GitOpsExplainException) {
                                errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                            }
                            throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                        }
                    });
        }

        private void addDevopsCustomResource (Map < String, String > objectPath, Long projectId, Long
        envId, List < DevopsCustomizeResource > devopsCustomizeResourceVOS, String path, Long userId){
            devopsCustomizeResourceVOS.stream()
                    .forEach(devopsCustomizeResourceVO -> {
                        String filePath = "";
                        try {
                            filePath = objectPath.get(TypeUtil.objToString(devopsCustomizeResourceVO.hashCode()));
                            io.choerodon.devops.infra.dto.DevopsCustomizeResourceDTO oldDevopsCustomizeResourceDTO = devopsCustomizeResourceService.queryByEnvIdAndKindAndName(envId, devopsCustomizeResourceVO.getK8sKind(), devopsCustomizeResourceVO.getName());

                            //初始化configMap参数,创建时判断configMap是否存在，存在则直接创建文件对象关联关系
                            if (oldDevopsCustomizeResourceDTO == null) {
                                devopsCustomizeResourceVO.setProjectId(projectId);
                                io.choerodon.devops.infra.dto.DevopsCustomizeResourceDTO customizeResourceDTO = ConvertUtils.convertObject(devopsCustomizeResourceVO, io.choerodon.devops.infra.dto.DevopsCustomizeResourceDTO.class);
                                devopsCustomizeResourceService.createOrUpdateResourceByGitOps(CREATE, customizeResourceDTO, userId, envId);
                                oldDevopsCustomizeResourceDTO = devopsCustomizeResourceService.queryByEnvIdAndKindAndName(envId, devopsCustomizeResourceVO.getK8sKind(), devopsCustomizeResourceVO.getName());
                            }
                            DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(oldDevopsCustomizeResourceDTO.getCommandId());
                            devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                            devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);

                            devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, null, devopsCustomizeResourceVO.hashCode(), oldDevopsCustomizeResourceDTO.getId(),
                                    ResourceType.CUSTOM.getType());

                        } catch (CommonException e) {
                            String errorCode = "";
                            if (e instanceof GitOpsExplainException) {
                                errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                            }
                            throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                        }
                    });
        }

}
