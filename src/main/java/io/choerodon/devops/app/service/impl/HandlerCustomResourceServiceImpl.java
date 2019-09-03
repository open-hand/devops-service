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
import io.choerodon.devops.infra.dto.DevopsCustomizeResourceContentDTO;
import io.choerodon.devops.infra.dto.DevopsCustomizeResourceDTO;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.mapper.DevopsCustomizeResourceMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Created by Sheep on 2019/7/1.
 */

@Service
public class HandlerCustomResourceServiceImpl implements HandlerObjectFileRelationsService<DevopsCustomizeResourceDTO> {

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
    @Autowired
    private DevopsCustomizeResourceMapper devopsCustomizeResourceMapper;

    @Override
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceDTO> beforeSync, List<DevopsCustomizeResourceDTO> devopsCustomizeResources, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<DevopsCustomizeResourceDTO> beforeDevopsCustomResource = beforeSync.stream()
                .filter(devopsEnvFileResourceVO -> devopsEnvFileResourceVO.getResourceType().equals(ResourceType.CUSTOM.getType()))
                .map(devopsEnvFileResourceVO -> {
                    DevopsCustomizeResourceDTO devopsCustomizeResourceDTO = devopsCustomizeResourceMapper.selectByPrimaryKey(devopsEnvFileResourceVO.getResourceId());
                    if (devopsCustomizeResourceDTO == null) {
                        devopsEnvFileResourceService
                                .baseDeleteByEnvIdAndResourceId(envId, devopsEnvFileResourceVO.getResourceId(), ResourceType.CUSTOM.getType());
                        return null;
                    }
                    return devopsCustomizeResourceDTO;
                }).collect(Collectors.toList());

        List<DevopsCustomizeResourceDTO> addCustomizeResourceDTOS = new ArrayList<>();
        List<DevopsCustomizeResourceDTO> updateCustomizeResourceDTOS = new ArrayList<>();
        devopsCustomizeResources.forEach(devopsCustomizeResourceDTO -> {
            if (beforeDevopsCustomResource.contains(devopsCustomizeResourceDTO)) {
                updateCustomizeResourceDTOS.add(devopsCustomizeResourceDTO);
                beforeDevopsCustomResource.remove(devopsCustomizeResourceDTO);
            } else {
                addCustomizeResourceDTOS.add(devopsCustomizeResourceDTO);
            }
        });

        //新增自定义资源
        addDevopsCustomResource(objectPath, projectId, envId, addCustomizeResourceDTOS, path, userId);
        //更新自定义资源
        updateDevopsCustomResource(objectPath, projectId, envId, updateCustomizeResourceDTOS, path, userId);
        //删除自定义资源
        beforeDevopsCustomResource.forEach(devopsCustomizeResourceDTO -> {
            DevopsCustomizeResourceDTO oldDevopsCustomizeResourceDTO = devopsCustomizeResourceService.queryByEnvIdAndKindAndName(envId, devopsCustomizeResourceDTO.getK8sKind(), devopsCustomizeResourceDTO.getName());
            if (oldDevopsCustomizeResourceDTO != null) {
                devopsCustomizeResourceService.deleteResourceByGitOps(oldDevopsCustomizeResourceDTO.getId());
                devopsEnvFileResourceService.baseDeleteByEnvIdAndResourceId(envId, devopsCustomizeResourceDTO.getId(), ResourceType.CUSTOM.getType());
            }
        });
    }


    private void updateDevopsCustomResource(Map<String, String> objectPath, Long projectId, Long
            envId, List<DevopsCustomizeResourceDTO> devopsCustomizeResourceDTOS, String path, Long userId) {
        devopsCustomizeResourceDTOS.forEach(customResource -> {
            String filePath = "";
            try {
                filePath = objectPath.get(TypeUtil.objToString(customResource.hashCode()));
                DevopsCustomizeResourceDTO devopsCustomizeResourceDTO = devopsCustomizeResourceService
                        .queryByEnvIdAndKindAndName(envId, customResource.getK8sKind(), customResource.getName());

                //判断自定义资源是否发生了改变
                DevopsCustomizeResourceContentDTO devopsCustomizeResourceContentDTO = devopsCustomizeResourceContentService.baseQuery(devopsCustomizeResourceDTO.getContentId());
                boolean isNotChange = customResource.getResourceContent().equals(devopsCustomizeResourceContentDTO.getContent());
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsCustomizeResourceDTO.getEnvId());

                //发生改变走处理改变自定义资源的逻辑
                if (!isNotChange) {
                    devopsCustomizeResourceDTO.setProjectId(projectId);
                    devopsCustomizeResourceDTO.setCommandId(devopsCustomizeResourceDTO.getContentId());
                    devopsCustomizeResourceService.createOrUpdateResourceByGitOps(UPDATE, devopsCustomizeResourceDTO, userId, envId);
                    DevopsCustomizeResourceDTO newDevopsCustomizeResourceDTO = devopsCustomizeResourceService
                            .queryByEnvIdAndKindAndName(envId, customResource.getK8sKind(), customResource.getName());
                    devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(newDevopsCustomizeResourceDTO.getCommandId());
                }

                //没发生改变,更新commit记录，更新文件对应关系记录
                devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());
                DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                        .baseQueryByEnvIdAndResourceId(envId, devopsCustomizeResourceDTO.getId(), ResourceType.CUSTOM.getType());
                devopsEnvFileResourceService.updateOrCreateFileResource(objectPath,
                        envId,
                        devopsEnvFileResourceDTO,
                        customResource.hashCode(), devopsCustomizeResourceDTO.getId(), ResourceType.CUSTOM.getType());

            } catch (CommonException e) {
                String errorCode = "";
                if (e instanceof GitOpsExplainException) {
                    errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                }
                throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
            }
        });
    }

    private void addDevopsCustomResource(Map<String, String> objectPath, Long projectId, Long
            envId, List<DevopsCustomizeResourceDTO> devopsCustomizeResourceDTOS, String path, Long userId) {
        devopsCustomizeResourceDTOS.forEach(customResource -> {
            String filePath = "";
            try {
                filePath = objectPath.get(TypeUtil.objToString(customResource.hashCode()));
                DevopsCustomizeResourceDTO oldDevopsCustomizeResourceDTO = devopsCustomizeResourceService.queryByEnvIdAndKindAndName(envId, customResource.getK8sKind(), customResource.getName());

                //初始化自定义资源参数,创建时判断自定义资源是否存在，存在则直接创建文件对象关联关系
                if (oldDevopsCustomizeResourceDTO == null) {
                    customResource.setProjectId(projectId);
                    DevopsCustomizeResourceDTO customizeResourceDTO = ConvertUtils.convertObject(customResource, DevopsCustomizeResourceDTO.class);
                    devopsCustomizeResourceService.createOrUpdateResourceByGitOps(CREATE, customizeResourceDTO, userId, envId);
                    oldDevopsCustomizeResourceDTO = devopsCustomizeResourceService.queryByEnvIdAndKindAndName(envId, customResource.getK8sKind(), customResource.getName());
                }
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(oldDevopsCustomizeResourceDTO.getCommandId());
                devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());

                devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, null, customResource.hashCode(), oldDevopsCustomizeResourceDTO.getId(),
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
