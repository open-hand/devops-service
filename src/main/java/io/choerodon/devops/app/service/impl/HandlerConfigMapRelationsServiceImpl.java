package io.choerodon.devops.app.service.impl;

import com.google.gson.Gson;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1Endpoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsConfigMapRespVO;
import io.choerodon.devops.api.vo.DevopsConfigMapVO;
import io.choerodon.devops.app.service.DevopsConfigMapService;
import io.choerodon.devops.app.service.DevopsEnvCommandService;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.app.service.HandlerObjectFileRelationsService;
import io.choerodon.devops.infra.dto.DevopsConfigMapDTO;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.GitOpsUtil;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;

@Service
public class HandlerConfigMapRelationsServiceImpl implements HandlerObjectFileRelationsService<V1ConfigMap> {

    private static final String CONFIG_MAP = "ConfigMap";
    private static final String GIT_SUFFIX = "/.git";
    private Gson gson = new Gson();

    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsConfigMapService devopsConfigMapService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;


    @Override
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceDTO> beforeSync, List<V1ConfigMap> v1ConfigMaps, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<String> beforeConfigMaps = beforeSync.stream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(CONFIG_MAP))
                .map(devopsEnvFileResourceE -> {
                    DevopsConfigMapDTO devopsConfigMapDTO = devopsConfigMapService
                            .baseQueryById(devopsEnvFileResourceE.getResourceId());
                    if (devopsConfigMapDTO == null) {
                        devopsEnvFileResourceService
                                .baseDeleteByEnvIdAndResourceId(envId, devopsEnvFileResourceE.getResourceId(), CONFIG_MAP);
                        return null;
                    }
                    return devopsConfigMapDTO.getName();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        //比较已存在configMap和新增要处理的configMap,获取新增configMap，更新configMap，删除configMap
        List<V1ConfigMap> addConfigMaps = new ArrayList<>();
        List<V1ConfigMap> updateConfigMaps = new ArrayList<>();
        GitOpsUtil.pickCUDResource(beforeConfigMaps, v1ConfigMaps, addConfigMaps, updateConfigMaps, configMap -> configMap.getMetadata().getName());

        //新增configMap
        addConfigMap(objectPath, envId, addConfigMaps, path, userId);
        //更新configMap
        updateConfigMap(objectPath, envId, updateConfigMaps, path, userId);
        //删除configMap,和文件对象关联关系
        beforeConfigMaps.forEach(configMapName -> {
            DevopsConfigMapDTO devopsConfigMapDTO = devopsConfigMapService.baseQueryByEnvIdAndName(envId, configMapName);
            if (devopsConfigMapDTO != null) {
                devopsConfigMapService.deleteByGitOps(devopsConfigMapDTO.getId());
                devopsEnvFileResourceService
                        .baseDeleteByEnvIdAndResourceId(envId, devopsConfigMapDTO.getId(), CONFIG_MAP);
            }
        });
    }

    @Override
    public Class<V1ConfigMap> getTarget() {
        return V1ConfigMap.class;
    }


    private void updateConfigMap(Map<String, String> objectPath, Long envId, List<V1ConfigMap> updateConfigMap, String path, Long userId) {
        updateConfigMap
                .forEach(configMap -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(configMap.hashCode()));
                        DevopsConfigMapDTO devopsConfigMapDTO = devopsConfigMapService
                                .baseQueryByEnvIdAndName(envId, configMap.getMetadata().getName());
                        //初始化configMap对象参数,更新configMap并更新文件对象关联关系
                        DevopsConfigMapVO devopsConfigMapVO = getDevospConfigMapDTO(
                                configMap,
                                envId, "update");
                        boolean isNotChange = devopsConfigMapVO.getValue().equals(gson.fromJson(devopsConfigMapService.baseQueryById(devopsConfigMapDTO.getId()).getValue(), Map.class));
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsConfigMapDTO.getCommandId());
                        devopsConfigMapVO.setId(devopsConfigMapDTO.getId());
                        if (!isNotChange) {
                            devopsConfigMapService.createOrUpdateByGitOps(devopsConfigMapVO, userId);
                            DevopsConfigMapDTO newDevopsConfigMapDTO = devopsConfigMapService
                                    .baseQueryByEnvIdAndName(envId, configMap.getMetadata().getName());
                            devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(newDevopsConfigMapDTO.getCommandId());
                        }
                        devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());
                        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                                .baseQueryByEnvIdAndResourceId(envId, devopsConfigMapDTO.getId(), configMap.getKind());
                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath,
                                envId,
                                devopsEnvFileResourceDTO,
                                configMap.hashCode(), devopsConfigMapDTO.getId(), configMap.getKind());

                    } catch (CommonException e) {
                        String errorCode = "";
                        if (e instanceof GitOpsExplainException) {
                            errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                        }
                        throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                    }
                });
    }

    private void addConfigMap(Map<String, String> objectPath, Long envId, List<V1ConfigMap> addConfigMap, String path, Long userId) {
        addConfigMap
                .forEach(configMap -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(configMap.hashCode()));
                        DevopsConfigMapDTO devopsConfigMapDTO = devopsConfigMapService
                                .baseQueryByEnvIdAndName(envId, configMap.getMetadata().getName());
                        DevopsConfigMapVO devopsConfigMapVO;

                        DevopsConfigMapRespVO devopsConfigMapRespVO = new DevopsConfigMapRespVO();
                        //初始化configMap参数,创建时判断configMap是否存在，存在则直接创建文件对象关联关系
                        if (devopsConfigMapDTO == null) {
                            devopsConfigMapVO = getDevospConfigMapDTO(
                                    configMap,
                                    envId,
                                    "create");
                            devopsConfigMapRespVO = devopsConfigMapService.createOrUpdateByGitOps(devopsConfigMapVO, userId);
                        } else {
                            devopsConfigMapRespVO.setId(devopsConfigMapDTO.getId());
                            devopsConfigMapRespVO.setCommandId(devopsConfigMapDTO.getCommandId());
                        }
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsConfigMapRespVO.getCommandId());
                        devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());

                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, null, configMap.hashCode(), devopsConfigMapRespVO.getId(),
                                configMap.getKind());
                    } catch (CommonException e) {
                        String errorCode = "";
                        if (e instanceof GitOpsExplainException) {
                            errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                        }
                        throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                    }
                });
    }


    private DevopsConfigMapVO getDevospConfigMapDTO(V1ConfigMap v1ConfigMap, Long envId, String type) {
        DevopsConfigMapVO devopsConfigMapVO = new DevopsConfigMapVO();
        devopsConfigMapVO.setDescription("");
        devopsConfigMapVO.setEnvId(envId);
        devopsConfigMapVO.setName(v1ConfigMap.getMetadata().getName());
        devopsConfigMapVO.setType(type);
        devopsConfigMapVO.setValue(v1ConfigMap.getData());
        return devopsConfigMapVO;
    }


}
