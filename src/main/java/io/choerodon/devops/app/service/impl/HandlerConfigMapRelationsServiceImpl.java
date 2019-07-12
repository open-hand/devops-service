package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsConfigMapVO;
import io.choerodon.devops.api.vo.DevopsConfigMapRepDTO;
import io.choerodon.devops.app.service.DevopsConfigMapService;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.api.vo.iam.entity.DevopsConfigMapE;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommandVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.domain.application.repository.DevopsConfigMapRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.app.service.HandlerObjectFileRelationsService;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;
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
                            .baseQueryById(devopsEnvFileResourceE.getResourceId());
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
            DevopsConfigMapE devopsConfigMapE = devopsConfigMapRepository.baseQueryByEnvIdAndName(envId, configMapName);
            if (devopsConfigMapE != null) {
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
                                .baseQueryByEnvIdAndName(envId, configMap.getMetadata().getName());
                        //初始化configMap对象参数,更新configMap并更新文件对象关联关系
                        DevopsConfigMapVO devopsConfigMapVO = getDevospConfigMapDTO(
                                configMap,
                                envId, "update");
                        Boolean isNotChange = devopsConfigMapVO.getValue().equals(gson.fromJson(devopsConfigMapRepository.baseQueryById(devopsConfigMapE.getId()).getValue(), Map.class));
                        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(devopsConfigMapE.getDevopsEnvCommandE().getId());
                        devopsConfigMapVO.setId(devopsConfigMapE.getId());
                        if (!isNotChange) {
                            devopsConfigMapService.createOrUpdateByGitOps(devopsConfigMapVO, userId);
                            DevopsConfigMapE newDevopsConfigMapE = devopsConfigMapRepository
                                    .baseQueryByEnvIdAndName(envId, configMap.getMetadata().getName());
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
                                .baseQueryByEnvIdAndName(envId, configMap.getMetadata().getName());
                        DevopsConfigMapVO devopsConfigMapVO = new DevopsConfigMapVO();

                        DevopsConfigMapRepDTO devopsConfigMapRepDTO = new DevopsConfigMapRepDTO();
                        //初始化configMap参数,创建时判断configMap是否存在，存在则直接创建文件对象关联关系
                        if (devopsConfigMapE == null) {
                            devopsConfigMapVO = getDevospConfigMapDTO(
                                    configMap,
                                    envId,
                                    "create");
                            devopsConfigMapRepDTO = devopsConfigMapService.createOrUpdateByGitOps(devopsConfigMapVO, userId);
                        } else {
                            devopsConfigMapRepDTO.setId(devopsConfigMapE.getId());
                            devopsConfigMapRepDTO.setCommandId(devopsConfigMapE.getDevopsEnvCommandE().getId());
                        }
                        DevopsEnvCommandVO devopsEnvCommandE = devopsEnvCommandRepository.query(devopsConfigMapRepDTO.getCommandId());
                        devopsEnvCommandE.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandRepository.update(devopsEnvCommandE);

                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, null, configMap.hashCode(), devopsConfigMapRepDTO.getId(),
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
