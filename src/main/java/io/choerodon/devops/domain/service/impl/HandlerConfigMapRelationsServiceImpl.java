package io.choerodon.devops.domain.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsConfigMapDTO;
import io.choerodon.devops.api.dto.DevopsConfigMapRepDTO;
import io.choerodon.devops.app.service.DevopsConfigMapService;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.domain.application.entity.DevopsConfigMapE;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommandE;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.handler.GitOpsExplainException;
import io.choerodon.devops.domain.application.repository.DevopsConfigMapRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.domain.service.HandlerObjectFileRelationsService;
import io.choerodon.devops.infra.common.util.GitUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.CommandStatus;
import io.choerodon.devops.infra.common.util.enums.CommandType;
import io.choerodon.devops.infra.common.util.enums.ObjectType;
import io.kubernetes.client.models.V1ConfigMap;
import io.kubernetes.client.models.V1Endpoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HandlerConfigMapRelationsServiceImpl implements HandlerObjectFileRelationsService<V1ConfigMap> {

    public static final String CONFIG_MAP = "ConfigMap";
    private static final String GIT_SUFFIX = "/.git";
    private Gson gson = new Gson();

    @Autowired
    private DevopsConfigMapRepository devopsConfigMapRepository;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private DevopsConfigMapService devopsConfigMapService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;


    @Override
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceE> beforeSync, List<V1ConfigMap> v1ConfigMaps, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<String> beforeConfigMaps = beforeSync.stream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(CONFIG_MAP))
                .map(devopsEnvFileResourceE -> {
                    DevopsConfigMapE devopsConfigMapE = devopsConfigMapRepository
                            .queryById(devopsEnvFileResourceE.getResourceId());
                    if (devopsConfigMapE == null) {
                        devopsEnvFileResourceRepository
                                .deleteByEnvIdAndResource(envId, devopsEnvFileResourceE.getResourceId(), CONFIG_MAP);
                        return null;
                    }
                    return devopsConfigMapE.getName();
                }).collect(Collectors.toList());

        //比较已存在configMap和新增要处理的configMap,获取新增configMap，更新configMap，删除configMap
        List<V1ConfigMap> addConfigMaps = new ArrayList<>();
        List<V1ConfigMap> updateConfigMaps = new ArrayList<>();
        v1ConfigMaps.stream().forEach(configMap -> {
            if (beforeConfigMaps.contains(configMap.getMetadata().getName())) {
                updateConfigMaps.add(configMap);
                beforeConfigMaps.remove(configMap.getMetadata().getName());
            } else {
                addConfigMaps.add(configMap);
            }
        });

        //新增configMap
        addConfigMap(objectPath, envId, addConfigMaps, path, userId);
        //更新configMap
        updateConfigMap(objectPath, envId, updateConfigMaps, path, userId);
        //删除configMap,和文件对象关联关系
        beforeConfigMaps.forEach(configMapName -> {
            DevopsConfigMapE devopsConfigMapE = devopsConfigMapRepository.queryByEnvIdAndName(envId, configMapName);
            if (devopsConfigMapE != null) {
                DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                        .query(devopsConfigMapE.getDevopsEnvCommandE().getId());
                if (!devopsEnvCommandE.getCommandType().equals(CommandType.DELETE.getType())) {
                    DevopsEnvCommandE devopsEnvCommandE1 = new DevopsEnvCommandE();
                    devopsEnvCommandE1.setCommandType(CommandType.DELETE.getType());
                    devopsEnvCommandE1.setObject(ObjectType.CONFIGMAP.getType());
                    devopsEnvCommandE1.setCreatedBy(userId);
                    devopsEnvCommandE1.setStatus(CommandStatus.OPERATING.getStatus());
                    devopsEnvCommandE1.setObjectId(devopsConfigMapE.getId());
                    devopsConfigMapE.initDevopsEnvCommandE(devopsEnvCommandRepository.create(devopsEnvCommandE1).getId());
                    devopsConfigMapRepository.update(devopsConfigMapE);
                }
                devopsConfigMapService.deleteByGitOps(devopsConfigMapE.getId());
                devopsEnvFileResourceRepository
                        .deleteByEnvIdAndResource(envId, devopsConfigMapE.getId(), CONFIG_MAP);
            }
        });
    }


    private void updateConfigMap(Map<String, String> objectPath, Long envId, List<V1ConfigMap> updateConfigMap, String path, Long userId) {
        updateConfigMap.stream()
                .forEach(configMap -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(configMap.hashCode()));
                        DevopsConfigMapE devopsConfigMapE = devopsConfigMapRepository
                                .queryByEnvIdAndName(envId, configMap.getMetadata().getName());
                        //初始化configMap对象参数,更新configMap并更新文件对象关联关系
                        DevopsConfigMapDTO devopsConfigMapDTO = getDevospConfigMapDTO(
                                configMap,
                                envId, "update");
                        Boolean isNotChange = devopsConfigMapDTO.getValue().equals(gson.fromJson(devopsConfigMapRepository.queryById(devopsConfigMapE.getId()).getValue(), Map.class));
                        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(devopsConfigMapE.getDevopsEnvCommandE().getId());
                        devopsConfigMapDTO.setId(devopsConfigMapE.getId());
                        if (!isNotChange) {
                            devopsConfigMapService.createOrUpdateByGitOps(devopsConfigMapDTO, userId);
                            DevopsConfigMapE newDevopsConfigMapE = devopsConfigMapRepository
                                    .queryByEnvIdAndName(envId, configMap.getMetadata().getName());
                            devopsEnvCommandE = devopsEnvCommandRepository.query(newDevopsConfigMapE.getDevopsEnvCommandE().getId());
                        }
                        devopsEnvCommandE.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandRepository.update(devopsEnvCommandE);
                        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                                .queryByEnvIdAndResource(envId, devopsConfigMapE.getId(), configMap.getKind());
                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath,
                                envId,
                                devopsEnvFileResourceE,
                                configMap.hashCode(), devopsConfigMapE.getId(), configMap.getKind());

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
        addConfigMap.stream()
                .forEach(configMap -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(configMap.hashCode()));
                        DevopsConfigMapE devopsConfigMapE = devopsConfigMapRepository
                                .queryByEnvIdAndName(envId, configMap.getMetadata().getName());
                        DevopsConfigMapDTO devopsConfigMapDTO = new DevopsConfigMapDTO();

                        DevopsConfigMapRepDTO devopsConfigMapRepDTO = new DevopsConfigMapRepDTO();
                        //初始化configMap参数,创建时判断configMap是否存在，存在则直接创建文件对象关联关系
                        if (devopsConfigMapE == null) {
                            devopsConfigMapDTO = getDevospConfigMapDTO(
                                    configMap,
                                    envId,
                                    "create");
                            devopsConfigMapRepDTO = devopsConfigMapService.createOrUpdateByGitOps(devopsConfigMapDTO, userId);
                        } else {
                            devopsConfigMapRepDTO.setId(devopsConfigMapE.getId());
                            devopsConfigMapRepDTO.setCommandId(devopsConfigMapE.getDevopsEnvCommandE().getId());
                        }
                        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(devopsConfigMapRepDTO.getCommandId());
                        devopsEnvCommandE.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandRepository.update(devopsEnvCommandE);
                        DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
                        devopsEnvFileResourceE.setEnvironment(new DevopsEnvironmentE(envId));
                        devopsEnvFileResourceE.setFilePath(objectPath.get(TypeUtil.objToString(configMap.hashCode())));
                        devopsEnvFileResourceE.setResourceId(devopsConfigMapRepDTO.getId());
                        devopsEnvFileResourceE.setResourceType(configMap.getKind());
                        devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
                    } catch (CommonException e) {
                        String errorCode = "";
                        if (e instanceof GitOpsExplainException) {
                            errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                        }
                        throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                    }
                });
    }


    private DevopsConfigMapDTO getDevospConfigMapDTO(V1ConfigMap v1ConfigMap, Long envId, String type) {
        DevopsConfigMapDTO devopsConfigMapDTO = new DevopsConfigMapDTO();
        devopsConfigMapDTO.setDescription("");
        devopsConfigMapDTO.setEnvId(envId);
        devopsConfigMapDTO.setName(v1ConfigMap.getMetadata().getName());
        devopsConfigMapDTO.setType(type);
        devopsConfigMapDTO.setValue(v1ConfigMap.getData());
        return devopsConfigMapDTO;
    }


}
